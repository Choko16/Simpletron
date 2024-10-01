/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.simv1;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class SimV1 {
    private static final int MEM_SIZE = 10000;
    private static final int PAGE_SIZE = 100;
    private static int[] memory = new int[MEM_SIZE];
    private static int acc = 0; // accumulator
    private static int IC = 0; // instruction counter
    private static int instructionRegister = 0;
    private static int indexRegister = 0;

    // Operation codes
    private static final int READ = 10;
    private static final int WRITE = 11;
    private static final int LOAD = 20;
    private static final int LOADIM = 21;
    private static final int LOADX = 22;
    private static final int LOADIDX = 23;
    private static final int STORE = 25;
    private static final int STOREIDX = 26;
    private static final int ADD = 30;
    private static final int ADDX = 31;
    private static final int SUBTRACT = 32;
    private static final int SUBTRACTX = 33;
    private static final int DIVIDE = 34;
    private static final int DIVIDEX = 35;
    private static final int MULTIPLY = 36;
    private static final int MULTIPLYX = 37;
    private static final int INC = 38;
    private static final int DEC = 39;
    private static final int BRANCH = 40;
    private static final int BRANCHNEG = 41;
    private static final int BRANCHZERO = 42;
    private static final int SWAP = 43;
    private static final int HALT = 45;

    private static Scanner inputScanner = new Scanner(System.in);

    public static void loadProgram() {
        System.out.println("*** Welcome to Simpletron V2! ***");
        System.out.print("Do you have a file that contains your SML program? (Y/N): ");
        String response = inputScanner.nextLine().trim().toUpperCase();
        
        if (response.equals("Y")) {
            System.out.print("Please enter the filename containing your SML program: ");
            String filepath = inputScanner.nextLine();
            loadProgramFromFile(filepath);
        } else {
            loadProgramFromKeyboard();
        }
    }

    private static void loadProgramFromFile(String filepath) {
        try (Scanner fileReader = new Scanner(new File(filepath))) {
            int location = 0;
            while (fileReader.hasNext() && location < PAGE_SIZE) {
                if (fileReader.hasNextInt()) {
                    int instruction = fileReader.nextInt();
                    if (isValidInstruction(instruction)) {
                        memory[location++] = instruction;
                    } else {
                        System.out.println("Invalid instruction: " + instruction);
                    }
                } else {
                    fileReader.next(); // Skip non-integer tokens
                }
            }
            System.out.println("Program loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void loadProgramFromKeyboard() {
        System.out.println("*** Please enter your program one instruction (or data word) at a time ***");
        System.out.println("*** Type -999999 to stop entering your program ***");
        
        int location = 0;
        while (location < PAGE_SIZE) {
            System.out.printf("%02d ? ", location);
            String input = inputScanner.nextLine().trim();
            
            if (input.equals("-999999")) {
                break;
            }
            
            try {
                int instruction = Integer.parseInt(input);
                if (isValidInstruction(instruction)) {
                    memory[location++] = instruction;
                } else {
                    System.out.println("Invalid instruction. Please enter a number between -999999 and +999999.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
        System.out.println("Program loaded successfully.");
    }

    private static boolean isValidInstruction(int instruction) {
        return instruction >= -999999 && instruction <= 999999;
    }

    public static void executeProgram() {
        System.out.println("*** Program execution begins ***");
        IC = 0;
        boolean running = true;
        while (running) {
            instructionRegister = memory[IC];
            int opCode = Math.abs(instructionRegister) / 10000;
            int operand = Math.abs(instructionRegister) % 10000;

            System.out.printf("Executing instruction at %04d: %+06d (OpCode: %02d, Operand: %04d)\n", 
                              IC, instructionRegister, opCode, operand);

            switch (opCode) {
                case READ:
                    System.out.print("? ");
                    int input = inputScanner.nextInt();
                    memory[operand] = input;
                    break;
                case WRITE:
                    System.out.println(memory[operand]);
                    break;
                case LOAD:
                    acc = memory[operand];
                    break;
                case LOADIM:
                    acc = instructionRegister % 1000000; // Preserve sign for immediate load
                    break;
                case LOADX:
                    indexRegister = memory[operand];
                    break;
                case LOADIDX:
                    acc = memory[indexRegister];
                    break;
                case STORE:
                    memory[operand] = acc;
                    break;
                case STOREIDX:
                    memory[indexRegister] = acc;
                    break;
                case ADD:
                    acc += memory[operand];
                    checkAccumulatorOverflow();
                    break;
                case ADDX:
                    acc += memory[indexRegister];
                    checkAccumulatorOverflow();
                    break;
                case SUBTRACT:
                    acc -= memory[operand];
                    checkAccumulatorOverflow();
                    break;
                case SUBTRACTX:
                    acc -= memory[indexRegister];
                    checkAccumulatorOverflow();
                    break;
                case DIVIDE:
                    if (memory[operand] == 0) throw new ArithmeticException("Division by zero");
                    acc /= memory[operand];
                    break;
                case DIVIDEX:
                    if (memory[indexRegister] == 0) throw new ArithmeticException("Division by zero");
                    acc /= memory[indexRegister];
                    break;
                case MULTIPLY:
                    acc *= memory[operand];
                    checkAccumulatorOverflow();
                    break;
                case MULTIPLYX:
                    acc *= memory[indexRegister];
                    checkAccumulatorOverflow();
                    break;
                case INC:
                    indexRegister++;
                    break;
                case DEC:
                    indexRegister--;
                    break;
                case BRANCH:
                    IC = operand - 1; // -1 because we increment IC at the end of the loop
                    break;
                case BRANCHNEG:
                    if (acc < 0) {
                        IC = operand - 1;
                    } else if (IC == 3) { // If we're at the BRANCHNEG instruction in the loop
                        IC = 0; // Go back to the start of the loop
                    }
                    break;
                case BRANCHZERO:
                    if (acc == 0) IC = operand - 1;
                    break;
                case SWAP:
                    int temp = acc;
                    acc = indexRegister;
                    indexRegister = temp;
                    break;
                case HALT:
                    System.out.println("*** Simpletron execution terminated ***");
                    coreDump(operand / 100, operand % 100);
                    running = false;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid operation code: " + opCode);
            }

            if (running) {
                IC++;
                if (IC >= PAGE_SIZE) {
                    IC = 0; // Loop back to the beginning if we reach the end of the page
                }
            }
        }
    }

    private static void checkAccumulatorOverflow() {
        if (acc > 999999 || acc < -999999) {
            throw new ArithmeticException("Accumulator overflow");
        }
    }

    private static void coreDump(int startPage, int endPage) {
        System.out.println("\nREGISTERS:");
        System.out.printf("accumulator          %+06d\n", acc);
        System.out.printf("instructionCounter   %04d\n", IC);
        System.out.printf("instructionRegister  %+06d\n", instructionRegister);
        System.out.printf("operationCode        %02d\n", Math.abs(instructionRegister) / 10000);
        System.out.printf("indexRegister        %+06d\n\n", indexRegister);

        System.out.println("MEMORY:");
        for (int page = startPage; page <= endPage; page++) {
            System.out.printf("PAGE # %02d\n\n", page);
            System.out.println("       0      1      2      3      4      5      6      7      8      9");
            for (int row = 0; row < 10; row++) {
                System.out.printf("%3d ", row * 10);
                for (int col = 0; col < 10; col++) {
                    int location = page * PAGE_SIZE + row * 10 + col;
                    System.out.printf("%06d ", memory[location]);
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        loadProgram();
        executeProgram();
    }
}