package net.speedstor.main;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


public class Input extends KeyAdapter{
	
	public Input() {
		
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		
		if(key == 13) System.out.println("Pressed Enter");
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		int key = e.getKeyCode();
		
		if(key == 13) System.out.println("Pressed Enter");
		
	}

}
