package org.hive2hive.core.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util for hashing and comparing hashes.
 * TODO: this could be parameterized with the security provider for an optimal result
 * 
 * @author Nico
 * @author Chris
 * 
 */
public class HashUtil {

	private static final Logger logger = LoggerFactory.getLogger(HashUtil.class);
	private static final String HASH_ALGORITHM = "SHA-256";

	private HashUtil() {
		// only static methods
	}

	/**
	 * Generates a hash of a given data
	 *
	 * @param data to calculate the hash over it
	 * @return the hash
	 */
	public static byte[] hash(byte[] data) {
		try {
			MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
			digest.update(data, 0, data.length);
			return digest.digest();
		} catch (NoSuchAlgorithmException e) {
			logger.error("Invalid hash algorithm {}", HASH_ALGORITHM, e);
			return new byte[0];
		}
	}

	/**
	 * Generates a hash of an input stream (can take a while)
	 *
	 * @param file the file to hash its contents
	 * @return the hash of the file
	 * @throws IOException if te file cannot be read
	 */
	public static byte[] hash(File file) throws IOException {
		if (file == null) {
			return new byte[0];
		} else if (file.isDirectory()) {
			return new byte[0];
		} else if (!file.exists()) {
			return new byte[0];
		}

		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(HASH_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Invalid hash algorithm {}", HASH_ALGORITHM, e);
			return new byte[0];
		}

		FileInputStream fis;
		try {
			// open the stream
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			logger.error("File {} not found to generate the hash", file, e);
			return new byte[0];
		}

		DigestInputStream dis = new DigestInputStream(fis, digest);
		try {
			byte[] buffer = new byte[1024];
			int numRead;
			do {
				numRead = dis.read(buffer);
			} while (numRead != -1);
		} finally {
			if (dis != null) {
				dis.close();
			}

			if (fis != null) {
				fis.close();
			}
		}

		return digest.digest();
	}

	/**
	 * Compares if the file hash matches a given hash
	 *
	 * @param file the file to comapre its hash
	 * @param expectedHash the expected hash of the file
	 * @return <code>true</code> if the file has the expected hash
	 * @throws IOException if the file cannot be read
	 */
	public static boolean compare(File file, byte[] expectedHash) throws IOException {
		if (!file.exists() && (expectedHash == null || expectedHash.length == 0)) {
			// both do not exist
			return true;
		} else if (file.isDirectory()) {
			// directories always match
			return true;
		}

		byte[] hash = HashUtil.hash(file);
		return compare(hash, expectedHash);
	}

	/**
	 * Compares if the given hash matches another hash. This method works symmetrically and is not
	 * dependent on the parameter order
	 *
	 * @param actual the hash to test
	 * @param expected the expected hash
	 * @return <code>true</code> if the hashes match
	 */
	public static boolean compare(byte[] actual, byte[] expected) {
		return Arrays.equals(actual, expected);
	}
}
