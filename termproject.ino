#include<LiquidCrystal.h>
#include "DHT.h"  // temp/humi sensor library
#include <SoftwareSerial.h> // bluetooth library

// LCD pins
int RS = 7;  // RS
int E = 8;   // E
int D4=2, D5=3, D6=5, D7=6; // D4,5,6,7 pins

// temp/humi sensor
int DHTpin = 11; // 센서 핀
DHT dht(DHTpin, DHT11);

// circulator pins
int INA = 9;  // circulator pin A
int INB = 10;  // circulator pin B

// bluetooth pins
int Tx=13; //transfer
int Rx=12; //receive

// input string from bluetooth
String inputString="";

LiquidCrystal lcd(RS, E, D4, D5, D6, D7);
//DHT11 dht11(DHTpin);
SoftwareSerial btSerial(Tx, Rx);

// circulating system ON/OFF
int sys = 0;

float hopeTemp = 100;
float hopeHumi = 100;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  pinMode(INA, OUTPUT);
  pinMode(INB, OUTPUT);
  btSerial.begin(9600);
  lcd.begin(16,2);  // 16row 2col
  dht.begin();

  // turn buzzer of motor shield off
  pinMode(4, OUTPUT);
  digitalWrite(4, LOW);
  // turn circulator off
  circulatorOFF();
}

void loop() {
  
  while(btSerial.available())
  {// bluetooth text receive
    char myChar = (char)btSerial.read();
    inputString += myChar;
    delay(5);
  }

  if(!inputString.equals(""))
  {// if input string is not empty
    Serial.println(inputString);
    if(inputString.charAt(1) == 'n')
    {// if input string is 'on'
      sys = 1;
      Serial.println("시스템 활성화");
    }
  }
  
  while(sys == 1)
  {// when system is turned on
    while(btSerial.available())
    {// bluetooth text receive
      char myChar = (char)btSerial.read();
      inputString += myChar;
      delay(5);
    }
    
    int err;  // error
    float temp, humi;   // temperature, humidity

    // read temperature & humidity from sensor
    temp = dht.readTemperature();
    humi = dht.readHumidity();

    // to catch failure of temp/humi sensor
    if (isnan(humi) || isnan(temp))
    {
      Serial.println("Failed to read from DHT sensor!");
      return;
    }
    
      // LCD print
      LCDprint(temp, humi);
      
      Serial.println(temp);
      Serial.println(humi);
          
      if(!inputString.equals(""))
      {// input text from bluetooth
        if(inputString.charAt(0) == 't')
        {// set minimum temperature
          hopeTemp = inputString.substring(4).toFloat();     
        } 
        else if(inputString.charAt(0) == 'h')
        {// set minimum humidity
          hopeHumi = inputString.substring(4).toFloat();    
        }
        else if(inputString.charAt(1) == 'f')
        {// to  turn this system off
          sys = 0;
          lcd.clear();
        }
     
        inputString = "";
      }
       
      if(temp > hopeTemp)
      {// if current temperature is higher than minimum temperature
        float dis = temp - hopeTemp;
        circulatorON(dis);
        temp = dht.readTemperature();
        humi = dht.readHumidity();
      }
      else
      {
        circulatorOFF();
      }

      if(humi > hopeHumi)
      {// if current temperature is higher than minimum humidity
        float dis = humi - hopeHumi;
        circulatorON(dis/10);
        temp = dht.readTemperature();
        humi = dht.readHumidity();
      }
      else
      {
        circulatorOFF();
      }
     
    delay(1500); //delay for reread
  }
  
}
void LCDprint(float temp, float humi)
{
  lcd.setCursor(0,0);
  lcd.print("temp : ");
  lcd.print(temp);
  lcd.setCursor(0,1);
  lcd.print("humi : ");
  lcd.print(humi);
}

void circulatorON(float dis)
{
  digitalWrite(INA, LOW);
  digitalWrite(INB, HIGH);
  delay(dis * 10000);
}

void circulatorOFF()
{
  digitalWrite(INA, LOW);
  digitalWrite(INB, LOW);
  delay(3000);
}
