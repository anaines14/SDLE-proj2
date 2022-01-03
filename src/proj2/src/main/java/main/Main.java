package main;

import visualization.MyGraphFrame;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Main{
    public static void main(String[] args) {
        //Graph thingys
        MyGraphFrame myGraphFrame = new MyGraphFrame();
        myGraphFrame.init();

        JFrame frame = new JFrame();
        frame.getContentPane().add(myGraphFrame);
        frame.setTitle("Graph YAY");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
