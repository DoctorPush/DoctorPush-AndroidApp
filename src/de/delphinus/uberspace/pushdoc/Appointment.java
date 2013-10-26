package de.delphinus.uberspace.pushdoc;

import java.util.Date;

/**
 * DoctorPush
 *
 * @author Shivan Taher <zn31415926535@gmail.com>
 * @date 27.10.13
 */
public class Appointment {
	private String name;
	private String localeDate;
	private Date date;

	public Appointment() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocaleDate() {
		return localeDate;
	}

	public void setLocaleDate(String localeDate) {
		this.localeDate = localeDate;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
