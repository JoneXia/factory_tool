package com.petkit.matetool.ui.common;

import java.util.Objects;
import java.util.TimeZone;

public class TimeZoneWrap {
    TimeZone timezone;
    String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeZoneWrap)) return false;
        TimeZoneWrap wrap = (TimeZoneWrap) o;
        return getName().equals(wrap.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    public TimeZoneWrap(TimeZone timezone, String name) {
        this.timezone = timezone;
        this.name = name;
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
