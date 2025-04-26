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
const long tempInterval = 60000; // 1 dakika

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
    else if (String(topic) == "valve/update_interval") {
        setUpdateInterval(message);
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
        
        // Sıcaklık ve nem verilerini JSON formatında gönder
        StaticJsonDocument<200> doc;
        
        float newTemp = dht.readTemperature();
        float outsideTemp = outsideDht.readTemperature();
        float humidity = outsideDht.readHumidity();
        
        if (!isnan(newTemp)) {
            currentTemp = newTemp;
            doc["temperature"] = currentTemp;
            doc["isHeating"] = isHeating;
        }
        
        if (!isnan(outsideTemp) && !isnan(humidity)) {
            doc["outsideTemperature"] = outsideTemp;
            doc["humidity"] = humidity;
        }
        
        doc["timestamp"] = millis();
        
        String jsonString;
        serializeJson(doc, jsonString);
        client.publish("valve/data", jsonString.c_str());
        
        checkHeating();
    }
}

void reconnect() {
    while (!client.connected()) {
        if (client.connect("ESP8266Client")) {
            client.subscribe("valve/target_temperature");
            client.subscribe("valve/schedules");
            client.subscribe("valve/update_interval");
        } else {
            delay(5000);
        }
    }
}

void setUpdateInterval(String message) {
    long newInterval = message.toInt() * 1000; // saniye cinsinden
    if (newInterval >= 10000 && newInterval <= 300000) { // 10 sn ile 5 dk arası
        tempInterval = newInterval;
    }
} 