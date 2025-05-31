import cv2
from picamera2 import Picamera2
from datetime import datetime
import requests
import firebase_admin
from firebase_admin import credentials, firestore
import os
import time
import serial  # For Arduino communication

# === Firebase Initialization ===
try:
    cred_path = "/home/pi4/firebase-service-account.json"
    if not os.path.exists(cred_path):
        raise FileNotFoundError(f"Firebase credential file not found at {cred_path}")
    
    cred = credentials.Certificate(cred_path)
    firebase_admin.initialize_app(cred)
    db = firestore.client()
    print("? Firebase initialized successfully")
except Exception as e:
    print(f"? Firebase initialization failed: {e}")
    exit(1)

# === Imgur API Setup ===
IMGUR_CLIENT_ID = 'f3197a5e7455512'
IMGUR_UPLOAD_URL = "https://api.imgur.com/3/image"
IMGUR_HEADERS = {'Authorization': f'Client-ID {IMGUR_CLIENT_ID}'}

# === Initialize Serial Communication with Arduino ===
try:
    ser = serial.Serial('/dev/ttyACM0', 9600)  # Adjust based on your port
    time.sleep(2)  # Allow time for the serial connection to establish
    print("? Serial communication established with Arduino")
except Exception as e:
    print(f"? Failed to initialize serial communication: {e}")
    exit(1)

# === Initialize Camera Using Picamera2 (with color fix) ===
try:
    picam2 = Picamera2()
    picam2.preview_configuration.main.size = (640, 480)
    picam2.preview_configuration.main.format = "RGB888"
    picam2.configure("preview")
    picam2.start()
    time.sleep(1)  # Allow camera to warm up
    print("? Camera initialized successfully")
except Exception as e:
    print(f"? Camera initialization failed: {e}")
    exit(1)

# === Load Face Cascade for Detection ===
face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + "haarcascade_frontalface_default.xml")

# === Main Loop for Capturing and Detecting Faces ===
def capture_and_process():
    try:
        # Capture frame and convert to correct color format
        frame = picam2.capture_array()
        frame = cv2.cvtColor(frame, cv2.COLOR_RGB2BGR)  # Fix blue tint by converting from RGB to BGR

        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

        # Detect faces
        faces = face_cascade.detectMultiScale(gray, 1.3, 5)

        for (x, y, w, h) in faces:
            # Draw professional face box (corner lines)
            line_len = int(w * 0.2)

            # Top-left corner
            cv2.line(frame, (x, y), (x + line_len, y), (0, 255, 0), 2)
            cv2.line(frame, (x, y), (x, y + line_len), (0, 255, 0), 2)

            # Top-right corner
            cv2.line(frame, (x + w, y), (x + w - line_len, y), (0, 255, 0), 2)
            cv2.line(frame, (x + w, y), (x + w, y + line_len), (0, 255, 0), 2)

            # Bottom-left corner
            cv2.line(frame, (x, y + h), (x + line_len, y + h), (0, 255, 0), 2)
            cv2.line(frame, (x, y + h), (x, y + h - line_len), (0, 255, 0), 2)

            # Bottom-right corner
            cv2.line(frame, (x + w, y + h), (x + w - line_len, y + h), (0, 255, 0), 2)
            cv2.line(frame, (x + w, y + h), (x + w, y + h - line_len), (0, 255, 0), 2)

            # Text for scanning indication
            cv2.putText(frame, "Scanning Face...", (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 0), 2)

        # Show the captured frame with face detection
        cv2.imshow("Face Scanner", frame)

        # Capture and process when Arduino sends "SCAN" signal
        if cv2.waitKey(1) == ord('q'):
            return False
        return True

    except Exception as e:
        print(f"Error in capture_and_process: {e}")
        return False

# === Main Loop for Serial Communication ===
def main():
    try:
        print("System ready. Waiting for Arduino trigger...")
        while True:
            if ser.in_waiting:
                line = ser.readline().decode().strip()
                print(f"Received from Arduino: {line}")
                
                if line == "SCAN":
                    print("Trigger received from Arduino, starting face capture...")
                    # Capture the image and upload to Imgur, Firestore
                    capture_and_process()

                    # Send response back to Arduino
                    ser.write(b"SUCCESS\n")
                    print("Operation completed successfully. Response sent to Arduino.")
            
            time.sleep(0.1)  # Prevent CPU overuse

    except KeyboardInterrupt:
        print("\nProgram stopped by user")
    except Exception as e:
        print(f"Unexpected error: {e}")
    finally:
        if ser and ser.is_open:
            ser.close()
        print("System cleanup complete")

if _name_ == "_main_":
    main()
