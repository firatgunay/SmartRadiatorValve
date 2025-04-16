#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <DHT.h>

#define DHTPIN 2
#define DHTTYPE DHT22
#define VALVE_PIN 4
#define OUTSIDE_DHTPIN 3  // Dış mekan sensörü için pin

const char* ssid = "WiFi_SSID";
const char* password = "WiFi_PASSWORD";
const char* mqtt_server = "broker.hivemq.com";

WiFiClient espClient;
PubSubClient client(espClient);
DHT dht(DHTPIN, DHTTYPE);
DHT outsideDht(OUTSIDE_DHTPIN, DHTTYPE);

float currentTemp = 0;
float targetTemp = 21;
bool isHeating = false;
unsigned long lastTemp = 0;
const long tempInterval = 30000; // 30 saniye

void setup() {
    Serial.begin(115200);
    pinMode(VALVE_PIN, OUTPUT);
    dht.begin();
    outsideDht.begin();
    
    setupWifi();
    setupMqtt();
}

void setupWifi() {
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.println("\nWiFi bağlandı");
}

void setupMqtt() {
    client.setServer(mqtt_server, 1883);
    client.setCallback(callback);
}

void callback(char* topic, byte* payload, unsigned int length) {
    String message = "";
    for (int i = 0; i < length; i++) {
        message += (char)payload[i];
    }
    
    if (String(topic) == "valve/target_temperature") {
        targetTemp = message.toFloat();
        checkHeating();
    }
    else if (String(topic) == "valve/schedules") {
        processSchedules(message);
    }
}

void processSchedules(String scheduleJson) {
    StaticJsonDocument<1024> doc;
    DeserializationError error = deserializeJson(doc, scheduleJson);
    
    if (error) {
        Serial.println("JSON parse hatası");
        return;
    }
    
    // Güncel saat ve güne göre hedef sıcaklığı ayarla
    int currentHour = hour();
    int currentDay = weekday(); // 1 = Pazar, 2-7 = Pazartesi-Cumartesi
    
    for (JsonVariant schedule : doc.as<JsonArray>()) {
        if (schedule["dayOfWeek"] == currentDay) {
            String startTime = schedule["startTime"];
            String endTime = schedule["endTime"];
            
            if (isTimeInRange(currentHour, startTime, endTime)) {
                targetTemp = schedule["targetTemperature"];
                checkHeating();
                break;
            }
        }
    }
}

void checkHeating() {
    if (currentTemp < targetTemp - 0.5) {
        isHeating = true;
        digitalWrite(VALVE_PIN, HIGH);
    } else if (currentTemp > targetTemp + 0.5) {
        isHeating = false;
        digitalWrite(VALVE_PIN, LOW);
    }
    
    client.publish("valve/status", isHeating ? "true" : "false");
}

void loop() {
    if (!client.connected()) {
        reconnect();
    }
    client.loop();
    
    unsigned long currentMillis = millis();
    if (currentMillis - lastTemp >= tempInterval) {
        lastTemp = currentMillis;
        
        float newTemp = dht.readTemperature();
        if (!isnan(newTemp)) {
            currentTemp = newTemp;
            
            char tempStr[10];
            dtostrf(currentTemp, 4, 2, tempStr);
            client.publish("valve/temperature", tempStr);
            
            checkHeating();
        }
        
        float outsideTemp = outsideDht.readTemperature();
        float humidity = outsideDht.readHumidity();
        
        if (!isnan(outsideTemp) && !isnan(humidity)) {
            char outsideTempStr[10];
            char humidityStr[10];
            dtostrf(outsideTemp, 4, 2, outsideTempStr);
            dtostrf(humidity, 4, 2, humidityStr);
            
            client.publish("valve/outside_temperature", outsideTempStr);
            client.publish("valve/humidity", humidityStr);
        }
    }
}

void reconnect() {
    while (!client.connected()) {
        if (client.connect("ESP8266Client")) {
            client.subscribe("valve/target_temperature");
            client.subscribe("valve/schedules");
        } else {
            delay(5000);
        }
    }
} 