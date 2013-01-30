package com.dianping.cosmos.hive.server.rijndael;

import java.security.InvalidKeyException;

public class Rijndael_Util {
	
	private static final int DEFAULT_BLOCK_SIZE = 32;

	public final static int getInt(byte[] bytes, int offSet) {
		return ((((bytes[offSet + 0] & 0xff) << 24)
				| ((bytes[offSet + 1]) << 16)
				| ((bytes[offSet + 2] & 0xff) << 8) | ((bytes[offSet + 3] & 0xff) << 0)));
	}

	public final static void putInt(int val, byte[] bytes, int offSet) {
		bytes[offSet] = (byte) (val >> 24);
		bytes[offSet + 1] = (byte) (val >> 16);
		bytes[offSet + 2] = (byte) (val >> 8);
		bytes[offSet + 3] = (byte) val;
	}

	public static String byte2String(byte[] bytes) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < bytes.length; ++i) {
			if (i != 0) {
				buff.append(", ");
			}
			buff.append(bytes[i]);
		}
		return buff.toString();
	}

	public static byte[] make_kb_16(String strKey) {
		return make_kb(strKey, 16);
	}

	public static byte[] make_kb_24(String strKey) {
		return make_kb(strKey, 24);
	}

	public static byte[] make_kb_32(String strKey) {
		return make_kb(strKey, 32);
	}

	private static byte[] make_kb(String strKey, int size) {
		byte[] kb = new byte[size];
		byte[] bytes = strKey.getBytes();
		System.arraycopy(bytes, 0, kb, 0, bytes.length > size ? size
				: bytes.length);
		return kb;
	}

	public static String encode(String key, String strData)
			throws InvalidKeyException {
		byte[] kb = make_kb_32(key);
		byte[] pt = strData.getBytes();
		byte[] ct = encode(kb, pt, DEFAULT_BLOCK_SIZE);
		return Base64.encode(ct);
	}

	public static String encode(String key, String strData, int blockSize)
			throws InvalidKeyException {
		byte[] kb = make_kb_32(key);
		byte[] pt = strData.getBytes();
		byte[] ct = encode(kb, pt, blockSize);
		return Base64.encode(ct);
	}

	public static String decode(String key, String strData)
			throws InvalidKeyException {
		byte[] kb = make_kb_32(key);
		byte[] ct = Base64.decode(strData);
		byte[] cpt = decode(kb, ct, DEFAULT_BLOCK_SIZE);
		return new String(cpt);
	}

	public static String decode(String key, String strData, int blockSize)
			throws InvalidKeyException {
		byte[] kb = make_kb_32(key);
		byte[] ct = Base64.decode(strData);
		byte[] cpt = decode(kb, ct, blockSize);
		return new String(cpt);
	}

	public static byte[] encode(byte[] kb, byte[] pt, int blockSize)
			throws InvalidKeyException {
		Object key = Rijndael_Algorithm.makeKey(kb, blockSize);

		int dataLength = pt.length;

		int mode = dataLength % blockSize;

		byte[] ct = new byte[mode == 0 ? pt.length : 8 + dataLength + blockSize
				- mode];

		for (int i = 0; i < dataLength; i += blockSize) {
			int restDataLength = dataLength - i;

			if (restDataLength >= blockSize) {
				byte[] block_ct = Rijndael_Algorithm.blockEncrypt(pt, i, key,
						blockSize);
				System.arraycopy(block_ct, 0, ct, i, blockSize);
			} else {
				if (blockSize == 16 || blockSize == 24) {
					Object last_block_key = Rijndael_Algorithm.makeKey(kb,
							blockSize + 8);
					byte[] block_pt = new byte[blockSize + 8];
					System.arraycopy(pt, i, block_pt, 0, restDataLength);
					putInt(dataLength, block_pt, blockSize);

					byte[] block_ct = Rijndael_Algorithm.blockEncrypt(block_pt,
							0, last_block_key, blockSize + 8);

					System.arraycopy(block_ct, 0, ct, i, blockSize + 8);
				} else { // 32
					Object key_16 = Rijndael_Algorithm.makeKey(kb, 16);
					byte[] block_24_pt = new byte[24];
					if (restDataLength > 16) {
						byte[] block_16_ct = Rijndael_Algorithm.blockEncrypt(
								pt, i, key_16, 16);
						restDataLength -= 16;
						System.arraycopy(block_16_ct, 0, ct, i, 16);

						System.arraycopy(pt, i + 16, block_24_pt, 0,
								restDataLength);
					} else {
						byte[] block_pt = new byte[16];
						System.arraycopy(pt, i, block_pt, 0, restDataLength);

						byte[] block_16_ct = Rijndael_Algorithm.blockEncrypt(
								block_pt, 0, key_16, 16);
						System.arraycopy(block_16_ct, 0, ct, i, 16);
					}

					Object key_24 = Rijndael_Algorithm.makeKey(kb, 24);

					putInt(dataLength, block_24_pt, 16);

					byte[] block_24_ct = Rijndael_Algorithm.blockEncrypt(
							block_24_pt, 0, key_24, 24);
					System.arraycopy(block_24_ct, 0, ct, i + 16, 24);
				}
			}
		}

		return ct;
	}

	public static byte[] decode(byte[] kb, byte[] ct, int blockSize)
			throws InvalidKeyException {

		int mode = ct.length % blockSize;

		Object key = Rijndael_Algorithm.makeKey(kb, blockSize);

		byte[] pt;
		if (mode == 0) {
			pt = new byte[ct.length];

			for (int i = 0; i < ct.length; i += blockSize) {
				byte[] block_pt = Rijndael_Algorithm.blockDecrypt(ct, i, key,
						blockSize);

				System.arraycopy(block_pt, 0, pt, i, blockSize);
			}
		} else {
			if (blockSize == 16 || blockSize == 24) {
				Object first_key = Rijndael_Algorithm
						.makeKey(kb, blockSize + 8);
				byte[] last_block_pt = Rijndael_Algorithm.blockDecrypt(ct,
						ct.length - blockSize - 8, first_key, blockSize + 8);

				int dataLength = getInt(last_block_pt, blockSize);

				pt = new byte[dataLength];
				System.arraycopy(last_block_pt, 0, pt, ct.length - blockSize
						- 8, dataLength % blockSize);
			} else { // 32
				Object key_24 = Rijndael_Algorithm.makeKey(kb, 24);
				byte[] last_block_24_pt = Rijndael_Algorithm.blockDecrypt(ct,
						ct.length - 24, key_24, 24);

				int dataLength = getInt(last_block_24_pt, 16);
				pt = new byte[dataLength];

				if (dataLength > ct.length - 24) {
					System.arraycopy(last_block_24_pt, 0, pt, ct.length - 24,
							dataLength - (ct.length - 24));
				}

				Object key_16 = Rijndael_Algorithm.makeKey(kb, 16);
				byte[] last_block_16_pt = Rijndael_Algorithm.blockDecrypt(ct,
						ct.length - 24 - 16, key_16, 16);
				if (pt.length > ct.length - 24) {
					System.arraycopy(last_block_16_pt, 0, pt,
							ct.length - 24 - 16, 16);
				} else {
					System.arraycopy(last_block_16_pt, 0, pt,
							ct.length - 24 - 16, pt.length
									- (ct.length - 24 - 16));
				}
			}

			for (int i = 0; i < ct.length - blockSize - 8; i += blockSize) {
				byte[] block_pt = Rijndael_Algorithm.blockDecrypt(ct, i, key,
						blockSize);
				System.arraycopy(block_pt, 0, pt, i, blockSize);
			}
		}

		return pt;
	}

}
