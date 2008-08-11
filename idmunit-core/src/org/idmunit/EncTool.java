/* 
 * IdMUnit - Automated Testing Framework for Identity Management Solutions
 * Copyright (c) 2005-2006 TriVir, LLC
 *
 * This program is licensed under the terms of the GNU General Public License
 * Version 2 (the "License") as published by the Free Software Foundation, and 
 * the TriVir Licensing Policies (the "License Policies").  A copy of the License 
 * and the Policies were distributed with this program.  
 *
 * The License is available at:
 * http://www.gnu.org/copyleft/gpl.html
 *
 * The Policies are available at:
 * http://www.idmunit.org/licensing/index.html
 *
 * Unless required by applicable law or agreed to in writing, this program is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied.  See the License and the Policies
 * for specific language governing the use of this program.
 *
 * www.TriVir.com
 * TriVir LLC
 * 11570 Popes Head View Lane
 * Fairfax, Virginia 22030
 *
 */
package org.idmunit;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.ddsteps.junit.behaviour.DdRowBehaviour;
import org.idmunit.connector.ConnectionConfigData;

/**
 * Provides FIPS encryption capability using the DES algorithm, as implemented in the Sun cryptography libraries.  
 * Used by IdMUnit to encrypt and decrypt password values so they are not stored in the clear.  Note that the key/salt used
 * by default for this encryption is IDMUNIT1.  This may be replaced by any other 8 byte value.  To replace the key, provide a new
 * value in the declaration of private final byte[] iv.  Then use this new key when encrypting passwords.   
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 */public class EncTool {
	  private final String xform = "DES/CBC/PKCS5Padding";
	  private final byte[] iv = { 0x0a, 0x01, 0x02, 0x03, 0x04, 0x0b, 0x0c, 0x0d };
	  private SecretKey secretKey;

	  /**
	   * Constructs an instance of the EncTool class and initializes the components required to perform encryption services.
	   * @param keyVal This value should be the same 8 byte value initialized in the private final byte[] iv declaration
	   * @throws IdMUnitException
	   */
	  public EncTool(String keyVal) throws IdMUnitException {
	    byte key[] = keyVal.getBytes();
	    try {
			DESKeySpec desKeySpec = new DESKeySpec(key);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			secretKey = keyFactory.generateSecret(desKeySpec);
		} catch (InvalidKeyException e) {
			throw new IdMUnitException("Encryption failed: Invalid Key: " + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			throw new IdMUnitException("Encryption failed: No such algorithm: " + e.getMessage());
		} catch (InvalidKeySpecException e) {
			throw new IdMUnitException("Encryption failed: Invalid Key spec: " + e.getMessage());
		}
	  }
	  
	  private byte[] encrypt(byte[] inpBytes,
	      SecretKey key, String xform) throws IdMUnitException {
	    try {
			Cipher cipher = Cipher.getInstance(xform);
			IvParameterSpec ips = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, key, ips);
			return cipher.doFinal(inpBytes);
		} catch (InvalidKeyException e) {
	    	throw new IdMUnitException("Failed to encrypt credentials: " + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
	    	throw new IdMUnitException("Failed to encrypt credentials: " + e.getMessage());
		} catch (NoSuchPaddingException e) {
	    	throw new IdMUnitException("Failed to encrypt credentials: " + e.getMessage());
		} catch (InvalidAlgorithmParameterException e) {
	    	throw new IdMUnitException("Failed to encrypt credentials: " + e.getMessage());
		} catch (IllegalStateException e) {
	    	throw new IdMUnitException("Failed to encrypt credentials: " + e.getMessage());
		} catch (IllegalBlockSizeException e) {
	    	throw new IdMUnitException("Failed to encrypt credentials: " + e.getMessage());
		} catch (BadPaddingException e) {
	    	throw new IdMUnitException("Failed to encrypt credentials: " + e.getMessage());
		}
	  }

	  private byte[] decrypt(byte[] inpBytes,
	      SecretKey key, String xform) throws IdMUnitException {
	    try {
			Cipher cipher = Cipher.getInstance(xform);
			IvParameterSpec ips = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, key, ips);
			return cipher.doFinal(inpBytes);
		} catch (InvalidKeyException e) {
	    	throw new IdMUnitException("Failed to decrypt credentials: " + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
	    	throw new IdMUnitException("Failed to decrypt credentials: " + e.getMessage());
		} catch (NoSuchPaddingException e) {
	    	throw new IdMUnitException("Failed to decrypt credentials: " + e.getMessage());
		} catch (InvalidAlgorithmParameterException e) {
	    	throw new IdMUnitException("Failed to decrypt credentials: " + e.getMessage());
		} catch (IllegalStateException e) {
	    	throw new IdMUnitException("Failed to decrypt credentials: " + e.getMessage());
		} catch (IllegalBlockSizeException e) {
	    	throw new IdMUnitException("Failed to decrypt credentials: " + e.getMessage());
		} catch (BadPaddingException e) {
	    	throw new IdMUnitException("Failed to decrypt credentials: " + e.getMessage());
		}
	  }
	  
	  /**
	   * Encrypts and base64-encodes a password for use in an idmunit-config.xml file
	   * @param password The password to be encrypted
	   * @return String The encrypted/encoded password buffer
	   * @throws IdMUnitException
	   */
	  public String encryptCredentials(String password) throws IdMUnitException {
	    try {
	        // Encode the string into bytes using utf-8
	        byte[] utf8 = password.getBytes("UTF8");

	        // Encrypt
	        byte[] enc = encrypt(utf8, secretKey, xform);

	        // Encode bytes to base64 to get a string
	        return new sun.misc.BASE64Encoder().encode(enc);
	    } catch (UnsupportedEncodingException e) {
	    	throw new IdMUnitException("Failed to encrypt credentials (Unsupported encoding)");
	    }
	}

	  /**
	   * Decrypts and base64-decodes a password for use during authentication to a target system
	   * @param password The encrypted/encoded password buffer
	   * @return String The password unencrypted
	   * @throws IdMUnitException
	   */
	public String decryptCredentials(String password) throws IdMUnitException {
	    try {
	        // Decode base64 to get bytes
	        byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(password);

	        // Decrypt
	        byte[] utf8 = decrypt(dec, secretKey, xform);

	        // Decode using utf-8
	        return new String(utf8, "UTF8");
	    } catch (UnsupportedEncodingException e) {
	    	throw new IdMUnitException("Failed to decrypt credentials (Unsupported encoding)");
	    } catch (java.io.IOException e) {
	    	throw new IdMUnitException("Failed to decrypt credentials (Bad I/O)");
	    }
	}

	private static void displayInstructions() {
		System.out.println("Usage: org.idmunit.EncTool keyVal myPassword");

	}
    
	/**
	 * This class may be instantiated directly with the arguments of String Key, String Password.  The 
	 * encrypted password value will be written to standard out.  This password may be copied/pasted into idmunit-config.xml.
	 * Note that the key used should be IDMUNIT1, unless the private final byte[] iv member variable of this class has been
	 * initialized to leverage a different key/salt.
	 * @param args
	 */
	public static void main(String[] args) {
    	System.out.println("IdMUnit " + Constants.VERSION);
    	if(args.length < 2) {
        	displayInstructions();
        	System.exit(-1);
    	} 
    	try {
			EncTool encryptionManager = new EncTool(args[0]);
			System.out.println("Encrypted password value: " + encryptionManager.encryptCredentials(args[1]));
    	} catch (IdMUnitException e) {
			System.out.println("Failed to encrypt password: " + e.getMessage());
		}
    	
    }

}
