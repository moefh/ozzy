
// === TYPES =========================

enum ProgramStatus {
  STATUS_INIT,
  STATUS_RUNNING,
};

struct PinConfig {
  byte pin;
  byte mode;
};

// === DATA ==========================

ProgramStatus status;
unsigned int step_counter;

byte input_buffer[64];
int input_buffer_size = 0;  // size of data in buffer
int input_buffer_read = 0;  // amount of buffer read

byte output_buffer[128];
int output_buffer_size = 0; // size of data in buffer

PinConfig pins[4];
const int NUM_PINS = (int) sizeof(pins)/sizeof(pins[0]);

unsigned long last_keepalive;
const unsigned long KEEPALIVE_LIMIT = 5000;
char cmd_ready[] = "\r\nAready\r\n";
char cmd_running[] = "Srunning\r\n";

// === CODE ==========================

void setup()
{
  pinMode(LED_BUILTIN, OUTPUT);

  Serial.begin(115200);
  status = STATUS_INIT;
  Serial.print(cmd_ready);
}

void loop()
{
  process_serial();
  check_connection();
  step_counter++;

  if (status == STATUS_RUNNING) {
    read_pins_and_send();
    delay(20);
    digitalWrite(LED_BUILTIN, (step_counter & 1) ? HIGH : LOW);
  } else {
    delay(20);
    digitalWrite(LED_BUILTIN, ((step_counter & 0x7f) >= 0x40) ? HIGH : LOW);
  }
}

void check_connection()
{
  if (status != STATUS_RUNNING) return;

  // this is more complicated than is looks in order to handle overflow
  unsigned long cur_millis = millis();
  unsigned long limit = last_keepalive + KEEPALIVE_LIMIT;
  if (last_keepalive < limit) {
    if (cur_millis < last_keepalive || cur_millis > limit) {
      status = STATUS_INIT;
    }
  } else {  // limit overflowed
    if (cur_millis < limit && limit < last_keepalive) {
      Serial.flush();
      status = STATUS_INIT;
    }
  }

  if (status == STATUS_INIT) {
    Serial.print(cmd_ready);
  }
}

void process_serial()
{
  while (Serial.available() > 0) {
    int b = Serial.read();
    if (b < 0) break;
    input_buffer[input_buffer_size++] = b;

    if (input_buffer_size >= 2 &&
        input_buffer[input_buffer_size-2] == '\r' &&
        input_buffer[input_buffer_size-1] == '\n') {
      input_buffer_size -= 2;
      process_command();
      input_buffer_size = 0;
    } else if (input_buffer_size == sizeof(input_buffer)) {
      process_command();
      input_buffer_size = 0;
    }
  }
}

void read_pins_and_send()
{
  int num_pins = 0;
  output_buffer_size = 0;
  output_byte('V');
  output_u32(millis());
  for (int i = 0; i < NUM_PINS; i++) {
    switch (pins[i].mode) {
      case 'A': output_byte('A'); output_u16(analogRead(pins[i].pin));               num_pins++; break;
      case 'D': output_byte('D'); output_byte(digitalRead(pins[i].pin) ? '1' : '0'); num_pins++; break;
    }
  }
  if (num_pins > 0) {
    output_byte('\r');
    output_byte('\n');
    Serial.write(output_buffer, output_buffer_size);
  }
}

void output_byte(byte b)
{
  output_buffer[output_buffer_size++] = b;
}

// there MUST be a more efficient to print an hex number without leading zeros
void output_u16(unsigned int number)
{
  unsigned int mask;
  unsigned int shift;
  if      (number > 0xfff) { mask = 0xf000; shift = 12; }
  else if (number > 0x0ff) { mask = 0x0f00; shift =  8; }
  else if (number > 0x00f) { mask = 0x00f0; shift =  4; }
  else                     { mask = 0x000f; shift =  0; }
  
  while (mask != 0) {
    unsigned int n = (number & mask) >> shift;
    mask >>= 4;
    shift -= 4;
    if (n < 10) {
      n += '0';
    } else {
      n += 'a' - 10;
    }
    output_buffer[output_buffer_size++] = n;
  }
}

void output_u32(unsigned long number)
{
  unsigned long mask;
  unsigned int shift;
  if      (number > 0xfffffff) { mask = 0xf0000000; shift = 28; }
  else if (number > 0x0ffffff) { mask = 0x0f000000; shift = 24; }
  else if (number > 0x00fffff) { mask = 0x00f00000; shift = 20; }
  else if (number > 0x000ffff) { mask = 0x000f0000; shift = 16; }
  else if (number > 0x0000fff) { mask = 0x0000f000; shift = 12; }
  else if (number > 0x00000ff) { mask = 0x00000f00; shift =  8; }
  else if (number > 0x000000f) { mask = 0x000000f0; shift =  4; }
  else                         { mask = 0x0000000f; shift =  0; }
  
  while (mask != 0) {
    unsigned int n = (number & mask) >> shift;
    mask >>= 4;
    shift -= 4;
    if (n < 10) {
      n += '0';
    } else {
      n += 'a' - 10;
    }
    output_buffer[output_buffer_size++] = n;
  }
}

unsigned int input_number()
{
  unsigned int num = 0;
  while (input_buffer_read < input_buffer_size) {
    byte c = input_buffer[input_buffer_read];
    if (c >= '0' && c <= '9') {
      num = (num<<4) + (c-'0');
    } else if (c >= 'a' && c <= 'f') {
      num = (num<<4) + (c-'a'+10);
    } else {
      break;
    }
    input_buffer_read++;
  }
  return num;
}

void process_command() {
  if (input_buffer[0] == 'G') {
    input_buffer_read = 1;
    int pin_index = 0;
    while (input_buffer_read < input_buffer_size && pin_index < NUM_PINS) {
      byte mode = input_buffer[input_buffer_read];
      if (mode == 'A' || mode == 'D') {
        input_buffer_read++;
        pins[pin_index].mode = mode;
        pins[pin_index].pin = input_number();
        pin_index++;
      } else {
        Serial.print("Ebad command format: ");
        Serial.write(input_buffer, input_buffer_size);
        Serial.println();
        return;
      }
    }
    setup_pins();
    status = STATUS_RUNNING;
    Serial.print(cmd_running);
    last_keepalive = millis();
    return;
  }

  if (input_buffer[0] == 'S') {
    status = STATUS_INIT;
    Serial.print(cmd_ready);
    return;
  }

  if (input_buffer[0] == 'K') {
    last_keepalive = millis();
    return;
  }

  if (input_buffer[0] == 'X') {
    output_buffer_size = 0;
    output_byte('C');
    output_u32(0x12345678);
    output_byte('\r');
    output_byte('\n');
    Serial.write(output_buffer, output_buffer_size);
    return;
  }
  
  Serial.print("Eunknown command: ");
  Serial.write(input_buffer, input_buffer_size);
  Serial.println();
}

void setup_pins()
{
  for (int i = 0; i < NUM_PINS; i++) {
    if (pins[i].mode == 'A' || pins[i].mode == 'D') {
      pinMode(pins[i].pin, INPUT);
    }
  }
}
