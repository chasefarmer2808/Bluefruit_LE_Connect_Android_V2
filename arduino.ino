/*********************************************************************
 This is an example for our nRF51 based Bluefruit LE modules

 Pick one up today in the adafruit shop!

 Adafruit invests time and resources providing this open source code,
 please support Adafruit and open-source hardware by purchasing
 products from Adafruit!

 MIT license, check LICENSE for more information
 All text above, and the splash screen below must be included in
 any redistribution
*********************************************************************/

// This sketch is intended to be used with the NeoPixel control
// surface in Adafruit's Bluefruit LE Connect mobile application.
//
// - Compile and flash this sketch to a board connected to/bundled with a nRF51
// - Open the Bluefruit LE Connect app
// - Switch to the NeoPixel utility
// - Click the 'connect' button to establish a connection and
//   send the meta-data about the pixel layout
// - Use the NeoPixel utility to update the pixels on your device

/* NOTE: This sketch required at least version 1.1.0 of Adafruit_Neopixel !!! */

#include <string.h>
#include <Arduino.h>
#include <SPI.h>
#include <Adafruit_NeoPixel.h>
#include "Adafruit_BLE.h"
#include "Adafruit_BluefruitLE_SPI.h"
#include "Adafruit_BluefruitLE_UART.h"
#if SOFTWARE_SERIAL_AVAILABLE
  #include <SoftwareSerial.h>
#endif

#include "BluefruitConfig.h"

#define FACTORYRESET_ENABLE     1
#define NEOPIXEL_VERSION_STRING "Neopixel v2.0"
#define PIN                     5   /* Pin used to drive the NeoPixels */

#define MAXCOMPONENTS  4
uint8_t *pixelBuffer = NULL;
uint8_t width = 0;
uint8_t height = 0;
uint8_t stride;
uint8_t componentsValue;
bool is400Hz;
uint8_t components = 3;     // only 3 and 4 are valid values

bool playAnimation = false;

Adafruit_NeoPixel neopixel = Adafruit_NeoPixel();

// Create the bluefruit object, either software serial...uncomment these lines
/*
SoftwareSerial bluefruitSS = SoftwareSerial(BLUEFRUIT_SWUART_TXD_PIN, BLUEFRUIT_SWUART_RXD_PIN);

Adafruit_BluefruitLE_UART ble(bluefruitSS, BLUEFRUIT_UART_MODE_PIN,
                      BLUEFRUIT_UART_CTS_PIN, BLUEFRUIT_UART_RTS_PIN);
*/

/* ...or hardware serial, which does not need the RTS/CTS pins. Uncomment this line */
// Adafruit_BluefruitLE_UART ble(BLUEFRUIT_HWSERIAL_NAME, BLUEFRUIT_UART_MODE_PIN);

/* ...hardware SPI, using SCK/MOSI/MISO hardware SPI pins and then user selected CS/IRQ/RST */
Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);

/* ...software SPI, using SCK/MOSI/MISO user-defined SPI pins and then user selected CS/IRQ/RST */
//Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_SCK, BLUEFRUIT_SPI_MISO,
//                             BLUEFRUIT_SPI_MOSI, BLUEFRUIT_SPI_CS,
//                             BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);


// A small helper
void error(const __FlashStringHelper*err) {
  Serial.println(err);
  while (1);
}

void serial_printf(const char * format, ...) {
  char buffer [48];
  va_list args;
  va_start(args, format);
  vsnprintf(buffer, sizeof(buffer), format, args);
  va_end(args);
  Serial.print(buffer);
}


/**************************************************************************/
/*!
    @brief  Sets up the HW an the BLE module (this function is called
            automatically on startup)
*/
/**************************************************************************/
void setup(void)
{
  Serial.begin(115200);
  Serial.println("Adafruit Bluefruit Neopixel Test");
  Serial.println("--------------------------------");

  Serial.println();
  Serial.println("Please connect using the Bluefruit Connect LE application");

  // Config Neopixels
  neopixel.begin();
  neopixel.clear();
  neopixel.setPixelColor(30, 255, 172, 68);
  neopixel.show();

  /* Initialise the module */
  Serial.print(F("Initialising the Bluefruit LE module: "));

  if ( !ble.begin(VERBOSE_MODE) )
  {
    error(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
  }
  Serial.println( F("OK!") );

  if ( FACTORYRESET_ENABLE )
  {
    /* Perform a factory reset to make sure everything is in a known state */
    Serial.println(F("Performing a factory reset: "));
    if ( ! ble.factoryReset() ){
      error(F("Couldn't factory reset"));
    }
  }

  /* Disable command echo from Bluefruit */
  ble.echo(false);

  ble.verbose(false);  // debug info is a little annoying after this point!

    /* Wait for connection */
  while (! ble.isConnected()) {
      delay(500);
  }

  Serial.println(F("***********************"));

  // Set Bluefruit to DATA mode
  Serial.println( F("Switching to DATA mode!") );
  ble.setMode(BLUEFRUIT_MODE_DATA);

  Serial.println(F("***********************"));
}

void loop()
{
  // Echo received data
  if ( ble.isConnected() )
  {
    int command = ble.read();

    handleCommand(command);
  }
}

void handleCommand(int command) {
  switch (command) {
    case 'V': {   // Get Version
      commandVersion();
      break;
    }
  
    case 'S': {   // Setup dimensions, components, stride...
      commandSetup();
      break;
    }

    case 'C': {   // Clear with color
      commandClearColor();
      break;
    }

    case 'B': {   // Set Brightness
      commandSetBrightness();
      break;
    }
            
    case 'P': {   // Set Pixel
      commandSetPixel();
      break;
    }

    case 'I': {   // Receive new image
      commandImage();
      break;
    }

    case 'R': {
      playAnimation = true;
      commandRainbowCycle(false);
      break;
    }

    case 'H': {
      playAnimation = true;
      commandTheaterChaseRainbow(false);
      break;
    }

    case 'A': {
      playAnimation = true;
      commandRandomPositionFill(200, false);
      break;
    }

    case 'M': {
      playAnimation = true;
      commandMeteorRain(10, 64, true, 30, false);
      break;
    }

    case 'F': {
      playAnimation = true;
      commandSideFillRandom(60, false);
      break;
    }

    case 'D': {
      playAnimation = true;
      commandSideFillRandom(60, true);
      commandRainbowCycle(true);
      commandTheaterChaseRainbow(true);
      commandRandomPositionFill(200, true);
      commandMeteorRain(10, 64, true, 30, true);
      handleCommand('D');
      break;
    }

    case 'T': {
      playAnimation = false;
      sendResponse("OK");
      break;
    }
  }
}

void swapBuffers()
{
  uint8_t *base_addr = pixelBuffer;
  int pixelIndex = 0;
  for (int j = 0; j < height; j++)
  {
    for (int i = 0; i < width; i++) {
      if (components == 3) {
        neopixel.setPixelColor(pixelIndex, neopixel.Color(*base_addr, *(base_addr+1), *(base_addr+2)));
      }
      else {
        neopixel.setPixelColor(pixelIndex, neopixel.Color(*base_addr, *(base_addr+1), *(base_addr+2), *(base_addr+3) ));
      }
      base_addr+=components;
      pixelIndex++;
    }
    pixelIndex += stride - width;   // Move pixelIndex to the next row (take into account the stride)
  }
  neopixel.show();

}

void commandVersion() {
  Serial.println(F("Command: Version check"));
  sendResponse(NEOPIXEL_VERSION_STRING);
}

void commandSetup() {
  Serial.println(F("Command: Setup"));

  width = ble.read();
  height = ble.read();
  stride = ble.read();
  componentsValue = ble.read();
  is400Hz = ble.read();

  neoPixelType pixelType;
  pixelType = componentsValue + (is400Hz ? NEO_KHZ400 : NEO_KHZ800);

  components = (componentsValue == NEO_RGB || componentsValue == NEO_RBG || componentsValue == NEO_GRB || componentsValue == NEO_GBR || componentsValue == NEO_BRG || componentsValue == NEO_BGR) ? 3:4;

  serial_printf("\tsize: %dx%d\n", width, height);
  serial_printf("\tstride: %d\n", stride);
  serial_printf("\tpixelType %d\n", pixelType);
  serial_printf("\tcomponents: %d\n", components);

  if (pixelBuffer != NULL) {
      delete[] pixelBuffer;
  }

  uint32_t size = width*height;
  pixelBuffer = new uint8_t[size*components];
  neopixel.updateLength(size);
  neopixel.updateType(pixelType);
  neopixel.setPin(PIN);

  // Done
  sendResponse("OK");
}

void commandSetBrightness() {
  Serial.println(F("Command: SetBrightness"));

   // Read value
  uint8_t brightness = ble.read();

  // Set brightness
  neopixel.setBrightness(brightness);

  // Refresh pixels
  swapBuffers();

  // Done
  sendResponse("OK");
}

void commandClearColor() {
  Serial.println(F("Command: ClearColor"));

  // Read color
  uint8_t color[MAXCOMPONENTS];
  for (int j = 0; j < components;) {
    if (ble.available()) {
      color[j] = ble.read();
      j++;
    }
  }

  // Set all leds to color
  int size = width * height;
  uint8_t *base_addr = pixelBuffer;
  for (int i = 0; i < size; i++) {
    for (int j = 0; j < components; j++) {
      *base_addr = color[j];
      base_addr++;
    }
  }

  // Swap buffers
  Serial.println(F("ClearColor completed"));
  swapBuffers();


  if (components == 3) {
    serial_printf("\tclear (%d, %d, %d)\n", color[0], color[1], color[2] );
  }
  else {
    serial_printf("\tclear (%d, %d, %d, %d)\n", color[0], color[1], color[2], color[3] );
  }
  
  // Done
  sendResponse("OK");
}

void commandSetPixel() {
  Serial.println(F("Command: SetPixel"));

  // Read position
  uint8_t x = ble.read();
  uint8_t y = ble.read();

  // Read colors
  uint32_t pixelOffset = y*width+x;
  uint32_t pixelDataOffset = pixelOffset*components;
  uint8_t *base_addr = pixelBuffer+pixelDataOffset;
  for (int j = 0; j < components;) {
    if (ble.available()) {
      *base_addr = ble.read();
      base_addr++;
      j++;
    }
  }

  // Set colors
  uint32_t neopixelIndex = y*stride+x;
  uint8_t *pixelBufferPointer = pixelBuffer + pixelDataOffset;
  uint32_t color;
  if (components == 3) {
    color = neopixel.Color( *pixelBufferPointer, *(pixelBufferPointer+1), *(pixelBufferPointer+2) );
    serial_printf("\tcolor (%d, %d, %d)\n",*pixelBufferPointer, *(pixelBufferPointer+1), *(pixelBufferPointer+2) );
  }
  else {
    color = neopixel.Color( *pixelBufferPointer, *(pixelBufferPointer+1), *(pixelBufferPointer+2), *(pixelBufferPointer+3) );
    serial_printf("\tcolor (%d, %d, %d, %d)\n", *pixelBufferPointer, *(pixelBufferPointer+1), *(pixelBufferPointer+2), *(pixelBufferPointer+3) );    
  }
  neopixel.setPixelColor(neopixelIndex, color);
  neopixel.show();

  // Done
  sendResponse("OK");
}

void commandImage() {
  serial_printf("Command: Image %dx%d, %d, %d\n", width, height, components, stride);
  
  // Receive new pixel buffer
  int size = width * height;
  uint8_t *base_addr = pixelBuffer;
  for (int i = 0; i < size; i++) {
    for (int j = 0; j < components;) {
      if (ble.available()) {
        *base_addr = ble.read();
        base_addr++;
        j++;
      }
    }

/*
    if (components == 3) {
      uint32_t index = i*components;
      Serial.printf("\tp%d (%d, %d, %d)\n", i, pixelBuffer[index], pixelBuffer[index+1], pixelBuffer[index+2] );
    }
    */
  }

  // Swap buffers
  Serial.println(F("Image received"));
  swapBuffers();

  // Done
  sendResponse("OK");
}

void commandRainbow() {
  uint16_t i, j;

  while (playAnimation) {
    for(j=0; j<256; j++) {
      for(i=0; i<neopixel.numPixels(); i++) {
        neopixel.setPixelColor(i, Wheel((i+j) & 255));

        handleCommand(ble.read());
        if (!playAnimation) {
          // Done
          sendResponse("OK");
          return;
        }
      }
      neopixel.show();
      delay(20);
    }
  }
}

void commandRainbowCycle(boolean single) {
  uint16_t i, j;

  while (playAnimation || single) {
    for(j=0; j<256*5; j++) { // 5 cycles of all colors on wheel
      for(i=0; i< neopixel.numPixels(); i++) {
        neopixel.setPixelColor(i, Wheel(((i * 256 / neopixel.numPixels()) + j) & 255));

        handleCommand(ble.read());
        if (!playAnimation) {
          // Done
          sendResponse("OK");
          return;
        }
      }

      neopixel.show();
      delay(20);
    }

    if (single) {
      break;
    }
  }
}

void commandTheaterChaseRainbow(boolean single) {
  while (playAnimation || single) {
    int firstPixelHue = 0;     // First pixel starts at red (hue 0)
    for(int a=0; a<30; a++) {  // Repeat 30 times...
      for(int b=0; b<3; b++) { //  'b' counts from 0 to 2...
        neopixel.clear();         //   Set all pixels in RAM to 0 (off)
        // 'c' counts up from 'b' to end of strip in increments of 3...
        for(int c=b; c<neopixel.numPixels(); c += 3) {
          // hue of pixel 'c' is offset by an amount to make one full
          // revolution of the color wheel (range 65536) along the length
          // of the strip (strip.numPixels() steps):
          int      hue   = firstPixelHue + c * 65536L / neopixel.numPixels();
          uint32_t color = neopixel.gamma32(neopixel.ColorHSV(hue)); // hue -> RGB
          neopixel.setPixelColor(c, color); // Set pixel 'c' to value 'color'

          handleCommand(ble.read());
          if (!playAnimation) {
            // Done
            sendResponse("OK");
            return;
          }
        }
        neopixel.show();                // Update strip with new contents
        delay(20);                 // Pause for a moment
        firstPixelHue += 65536 / 90; // One cycle of color wheel over 90 frames
      }
    }

    if (single) {
      break;
    }
  }
}

void commandRandomPositionFill(uint8_t wait, boolean single) {
  while (playAnimation || single) {
    clearStrip();

    int filled[neopixel.numPixels()];
    int fillCount = 0;

    for (int i = 0; i < neopixel.numPixels(); i++) {
      filled[i] = 0;
    }

    while(fillCount < neopixel.numPixels() - 1) {
      handleCommand(ble.read());
      if (!playAnimation) {
        // Done
        sendResponse("OK");
        return;
      }
      
      int currPixel = random(0, neopixel.numPixels() - 1);

      if (filled[currPixel] != 1) {
        neopixel.setPixelColor(currPixel, getRandomRGB());
        filled[currPixel] = 1;
        fillCount++;
        neopixel.show();
        delay(wait);
      }
    }

    if (single) {
      break;
    }
  }
}

void commandMeteorRain(byte meteorSize, byte meteorTrailDecay, boolean meteorRandomDecay, int speedDelay, boolean single) {
  while (playAnimation || single) {
    clearStrip();
    uint32_t color = getRandomRGB();

    int numPixels = neopixel.numPixels();

    for (int i = 0; i < numPixels+numPixels; i++) {
      
      // Fade brightness of all LEDs.
      for (int j = 0; j < numPixels; j++) {
        if ((!meteorRandomDecay) || (random(10) < 5)) {
          fadeToBlack(j, meteorTrailDecay);

          handleCommand(ble.read());
          if (!playAnimation) {
            // Done
            sendResponse("OK");
            return;
          }
        }
      }

      // Draw meteor.
      for (int j = 0; j < meteorSize; j++) {
        if ((i - j < numPixels) && (i - j >= 0)) {
          neopixel.setPixelColor(i - j, color);

          handleCommand(ble.read());
          if (!playAnimation) {
            // Done
            sendResponse("OK");
            return;
          }
        }
      }

      neopixel.show();
      delay(speedDelay);
    }

    if (single) {
      break;
    }
  }
}

void commandSideFillRandom(uint8_t wait, boolean single) {
  while (playAnimation || single) {
    clearStrip();
    uint32_t c = getRandomRGB();
  
    for(uint16_t i=0; i<(neopixel.numPixels()/2); i++) { // fill strip from sides to middle
      neopixel.setPixelColor(i, c);
      neopixel.setPixelColor(neopixel.numPixels() - i, c);
      neopixel.show();
      delay(wait);

      handleCommand(ble.read());
      if (!playAnimation) {
        // Done
        sendResponse("OK");
        return;
      }
    }
  
    for(uint16_t i=0; i<(neopixel.numPixels()/2); i++) { // reverse
      neopixel.setPixelColor(neopixel.numPixels()/2 + i, neopixel.Color(0, 0, 0));
      neopixel.setPixelColor(neopixel.numPixels()/2 - i, neopixel.Color(0, 0, 0));
      neopixel.show();
      delay(wait);

      handleCommand(ble.read());
      if (!playAnimation) {
        // Done
        sendResponse("OK");
        return;
      }
    }

    if (single) {
      break;
    }
  }
}

void fadeToBlack(int pixelNum, byte fadeValue) {
  uint32_t oldColor;
  uint8_t r, g, b;
  int value;

  oldColor = neopixel.getPixelColor(pixelNum);
  r = (oldColor & 0x00ff0000UL) >> 16;
  g = (oldColor & 0x0000ff00UL) >> 8;
  b = (oldColor & 0x000000ffUL);

  r = (r<=10)? 0 : (int) r-(r*fadeValue/256);
  g = (g<=10)? 0 : (int) g-(g*fadeValue/256);
  b = (b<=10)? 0 : (int) b-(b*fadeValue/256);

  neopixel.setPixelColor(pixelNum, r, g, b);
}

void setStripRGBW(uint8_t r, uint8_t g, uint8_t b, uint8_t w) {
  for (int i = 0; i < neopixel.numPixels(); i++) {
    neopixel.setPixelColor(i, neopixel.Color(r, g, b, w));
  }
  neopixel.show();
}

void clearStrip() {
  for (int i = 0; i < neopixel.numPixels(); i++) {
    neopixel.setPixelColor(i, 0, 0, 0);
  }
  neopixel.show();
}

uint32_t getRandomRGB() {
  int r = random(0, 255);
  int g = random(0, 255);
  int b = random(0, 255);

  return neopixel.Color(r, g, b);
}

uint32_t getRandomRGBW() {
  int r = random(0, 255);
  int g = random(0, 255);
  int b = random(0, 255);
  int w = random(0, 255);

  return neopixel.Color(r, g, b, w);
}

// Input a value 0 to 255 to get a color value.
// The colours are a transition r - g - b - back to r.
uint32_t Wheel(byte WheelPos) {
  WheelPos = 255 - WheelPos;
  if(WheelPos < 85) {
    return neopixel.Color(255 - WheelPos * 3, 0, WheelPos * 3);
  }
  if(WheelPos < 170) {
    WheelPos -= 85;
    return neopixel.Color(0, WheelPos * 3, 255 - WheelPos * 3);
  }
  WheelPos -= 170;
  return neopixel.Color(WheelPos * 3, 255 - WheelPos * 3, 0);
}

void sendResponse(char const *response) {
    serial_printf("Send Response: %s\n", response);
    ble.write(response, strlen(response)*sizeof(char));
}
