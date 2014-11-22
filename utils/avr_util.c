/** 
 * AVR program for control of the Ball and Beam process.
 * 
 * * To compile for the ATmega8 AVR:
 *   avr-gcc -mmcu=atmega8 -O -g -Wall -o avr_util.o avr_util.c   
 * 
 * * To compile for the ATmega16 AVR:
 *   avr-gcc -mmcu=atmega16 -O -g -Wall -o avr_util.o avr_util.c   
 * 
 * To upload to the AVR:
 * avr-objcopy -Osrec avr_util.o avr_util.sr
 * uisp -dprog=stk200 --erase --upload if=avr_util.sr
 * 
 * To view the assembler code:
 * avr-objdump -S avr_util.o
 * 
 * To open a serial terminal on the PC:
 * simcom -38400 /dev/ttyS0 
 */
#include <avr/io.h>
#include <avr/interrupt.h>

#define angular_read  1
#define ball_pos_read 0
#define true          1
#define false         0

/* Variables */
int8_t  on = 0;                     /* 0=off, 1=on */
int16_t r  = 255;                   /* Reference, corresponds to +5.0 V */
int16_t angle_write = 0;
int16_t angle_read  = 0;
int16_t ball_read   = 0;
int16_t buffer[8]   = {0};
uint8_t bias        = 0;

/** 
 * Write a character on the serial connection
 */
static inline void put_char(char ch){
    while ((UCSRA & 0x20) == 0){};
    UDR = ch;
}

/**
 * Write 10-bit output using the PWM generator.
 */
static inline void writeOutput(int16_t val){
    val   += 512;
    OCR1AH = (uint8_t) (val>>8);
    OCR1AL = (uint8_t) val;
}

/**
 * Read 10-bit input using the AD converter
 */
static inline int16_t readInput(char chan){
  
    uint8_t low  = 0;
    uint8_t high = 0;

    ADMUX   = 0xc0 + chan;             /* Specify channel (0 or 1) */
    ADCSRA |= 0x40;                    /* Start the conversion */
    while (ADCSRA & 0x40);             /* Wait for conversion to finish */
        low = ADCL;                    /* Read input, low byte first! */
        high = ADCH;                   /* Read input, high byte */
    return ((high<<8) | low) - 512;    /* 10 bit ADC value [-512..511] */ 
}  

/**
 * Interrupt handler for receiving characters over serial connection
 */
ISR(USART_RXC_vect){
    if(angle_input){
        if(UDR == '.'){
            bias = 0;
            writeOutput((int16_t)atoi(buffer));
        }else{     
            buffer[bias++] = UDR;
        }
    } 
    switch (UDR){
        case 's':                        /* Start the controller */
            on = 1;
            break;
        case 't':                        /* Stop the controller */
            on = 0;
            writeOutput(0);              /* Off */
            break;
        case 'r':                        /* Change sign of reference */
            r = -r;
            break;
        case 'a':                        /* Reads angular input from rasp */
            angle_input = true;          /* State start to read angular input from rasp */   
    }
}

/**
 * Interrupt handler for the periodic timer. Interrupts are generated
 * every 10 ms.
 */
ISR(TIMER2_COMP_vect){

    static int8_t ctr = 0;

    if (++ctr < 5) return;
    ctr = 0;
    if (on){
        angle_read = readInput(angular_read);
        ball_read  = readInput(ball_pos_read);
        //int_16 to string write stdout to rasp
        
    }
}

/**
 * Main program
 */
int main(){

    DDRB = 0x02;    /* Enable PWM output for ATmega8 */
    DDRD = 0x20;    /* Enable PWM output for ATmega16 */
    DDRC = 0x30;    /* Enable time measurement pins */
    ADCSRA = 0xc7;  /* ADC enable */

    TCCR1A = 0xf3;  /* Timer 1: OC1A & OC1B 10 bit fast PWM */
    TCCR1B = 0x09;  /* Clock / 1 */

    TCNT2 = 0x00;   /* Timer 2: Reset counter (periodic timer) */
    TCCR2 = 0x0f;   /* Clock / 1024, clear after compare match (CTC) */
    OCR2 = 144;     /* Set the compare value, corresponds to ~100 Hz */

    /* Configure serial communication */
    UCSRA = 0x00;   /* USART: */
    UCSRB = 0x98;   /* USART: RXC enable, Receiver enable, Transmitter enable */
    UCSRC = 0x86;   /* USART: 8bit, no parity */
    UBRRH = 0x00;   /* USART: 38400 @ 14.7456MHz */
    UBRRL = 23;     /* USART: 38400 @ 14.7456MHz */

    TIMSK = 1<<OCIE2; /* Start periodic timer */

    sei();          /* Enable interrupts */

    while (1);
}

