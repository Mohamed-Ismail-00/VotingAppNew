#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <Keypad.h>

// LCD Configuration (0x27 or 0x3F)
LiquidCrystal_I2C lcd(0x27, 16, 2);

// Timing control
unsigned long lastActionTime = 0;
const unsigned long messageTimeout = 3000; // 3 seconds
bool showingTempMessage = false;

// Keypad Configuration
const byte ROWS = 4;
const byte COLS = 4;
char keys[ROWS][COLS] = {
  {'1','2','3','A'},
  {'4','5','6','B'},
  {'7','8','9','C'},
  {'*','0','#','D'}
};

byte rowPins[ROWS] = {9, 8, 7, 6};
byte colPins[COLS] = {5, 4, 3, 2};
Keypad keypad = Keypad(makeKeymap(keys), rowPins, colPins, ROWS, COLS);

// Button Configuration
const int buttonPin = 2;
const int irLedPin = 10; // IR LED connected to pin 10 (change as needed)
int buttonState = HIGH; // Assuming pull-up (LOW when pressed)
int lastButtonState = HIGH;
unsigned long lastDebounceTime = 0;
const unsigned long debounceDelay = 50;

void showDefaultScreen(); 

void setup() {
  lcd.init();
  lcd.backlight();
  showDefaultScreen();
  
  pinMode(buttonPin, INPUT_PULLUP);
  pinMode(irLedPin, OUTPUT); // Set IR LED pin as output
  digitalWrite(irLedPin, LOW); // Ensure IR LED starts off
  Serial.begin(9600);

  // raspberry
  pinMode(2, INPUT_PULLUP); // push button
}

void loop() {
  // Check if temporary message should be cleared
  checkMessageTimeout();

  // Handle keypad input
  handleKeypad();
  
  // Handle button press
  handleButton();

  // raspberry
  if (digitalRead(buttonPin) == LOW) {
    Serial.println("SCAN");
    delay(1000);
  }

  // not repeat
  if (digitalRead(2) == LOW) {
    Serial.println("start");
    delay(2000);
  }
}

void checkMessageTimeout() {
  if (showingTempMessage && (millis() - lastActionTime >= messageTimeout)) {
    showDefaultScreen();
    showingTempMessage = false;
  }
}

void handleKeypad() {
  char key = keypad.getKey();
  if (key) {
    showTemporaryMessage(String("select: ") + key);
    Serial.print("Key pressed: ");
    Serial.println(key);
  }
}

void handleButton() {
  int reading = digitalRead(buttonPin);
  
  if (reading != lastButtonState) {
    lastDebounceTime = millis();
  }
  
  if ((millis() - lastDebounceTime) > debounceDelay) {
    if (reading != buttonState) {
      buttonState = reading;
      // ir led 
      if (buttonState == LOW) { // Button pressed
        showTemporaryMessage("Scanning...");
        
        // Turn on IR LED after a brief delay
        delay(100); // Small delay before turning on IR LED
        digitalWrite(irLedPin, HIGH); // Turn on IR LED
        
        delay(5000); //  el camera bt3ml scan (5 seconds)
        
        digitalWrite(irLedPin, LOW); // Turn off IR LED after scanning
        showTemporaryMessage("Select a\ncandidate");
      }
    }
  }
  
  lastButtonState = reading;
}

void showTemporaryMessage(String message) {
  lcd.clear();
  
  // Handle multi-line messages (split at \n)
  int newlinePos = message.indexOf('\n');
  if (newlinePos == -1) {
    // Single line message
    lcd.print(message);
  } else {
    // Multi-line message
    lcd.print(message.substring(0, newlinePos));
    lcd.setCursor(0, 1);
    lcd.print(message.substring(newlinePos + 1));
  }
  
  lastActionTime = millis();
  showingTempMessage = true;
}

void showDefaultScreen() {
  lcd.clear();
  lcd.print("Voting system");
  lcd.setCursor(0, 1);
  lcd.print("push a button");
}
