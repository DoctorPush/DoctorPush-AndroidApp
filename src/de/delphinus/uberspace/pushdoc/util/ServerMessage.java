package de.delphinus.uberspace.pushdoc.util;

/**
 * DoctorPush
 *
 * @author Shivan Taher <zn31415926535@gmail.com>
 * @date 26.10.13
 */
public class ServerMessage {

	public static String user(String registrationId, String phoneNumber) {
		return "{\"androidDeviceID\": \""+registrationId+"\", \"phoneNumber\": \""+phoneNumber+"\"}";
	}

}
