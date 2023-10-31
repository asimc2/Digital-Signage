package com.cleartwo.tvapplication;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@Keep
public class ReqResponse {

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("data")
    @Expose
    private String data;
    @SerializedName("schedule")
    @Expose
    private Schedule schedule;
    @SerializedName("playlists")
    @Expose
    private List<Playlist> playlists = null;
    @SerializedName("defaultplaylist")
    @Expose
    private Defaultplaylist defaultplaylist;
    @SerializedName("Success")
    @Expose
    private Boolean success;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public List<Playlist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(List<Playlist> playlists) {
        this.playlists = playlists;
    }

    public Defaultplaylist getDefaultplaylist() {
        return defaultplaylist;
    }

    public void setDefaultplaylist(Defaultplaylist defaultplaylist) {
        this.defaultplaylist = defaultplaylist;
    }

    @Keep
    public class Schedule {

        @SerializedName("Monday")
        @Expose
        private List<Monday> monday = null;
        @SerializedName("Tuesday")
        @Expose
        private List<Tuesday> tuesday = null;
        @SerializedName("Wednesday")
        @Expose
        private List<Wednesday> wednesday = null;
        @SerializedName("Thursday")
        @Expose
        private List<Thursday> thursday = null;
        @SerializedName("Friday")
        @Expose
        private List<Friday> friday = null;
        @SerializedName("Saturday")
        @Expose
        private List<Saturday> saturday = null;
        @SerializedName("Sunday")
        @Expose
        private List<Sunday> sunday = null;

        public List<Monday> getMonday() {
            return monday;
        }

        public void setMonday(List<Monday> monday) {
            this.monday = monday;
        }

        public List<Tuesday> getTuesday() {
            return tuesday;
        }

        public void setTuesday(List<Tuesday> tuesday) {
            this.tuesday = tuesday;
        }

        public List<Wednesday> getWednesday() {
            return wednesday;
        }

        public void setWednesday(List<Wednesday> wednesday) {
            this.wednesday = wednesday;
        }

        public List<Thursday> getThursday() {
            return thursday;
        }

        public void setThursday(List<Thursday> thursday) {
            this.thursday = thursday;
        }

        public List<Friday> getFriday() {
            return friday;
        }

        public void setFriday(List<Friday> friday) {
            this.friday = friday;
        }

        public List<Saturday> getSaturday() {
            return saturday;
        }

        public void setSaturday(List<Saturday> saturday) {
            this.saturday = saturday;
        }

        public List<Sunday> getSunday() {
            return sunday;
        }

        public void setSunday(List<Sunday> sunday) {
            this.sunday = sunday;
        }

    }

    @Keep
    public class Sunday {

        @SerializedName("start")
        @Expose
        private Start start;
        @SerializedName("end")
        @Expose
        private End end;
        @SerializedName("playlist")
        @Expose
        private Playlist playlist;

        public Start getStart() {
            return start;
        }

        public void setStart(Start start) {
            this.start = start;
        }

        public End getEnd() {
            return end;
        }

        public void setEnd(End end) {
            this.end = end;
        }

        public Playlist getPlaylist() {
            return playlist;
        }

        public void setPlaylist(Playlist playlist) {
            this.playlist = playlist;
        }

    }

    @Keep
    public class Thursday {

        @SerializedName("start")
        @Expose
        private Start start;
        @SerializedName("end")
        @Expose
        private End end;
        @SerializedName("playlist")
        @Expose
        private Playlist playlist;

        public Start getStart() {
            return start;
        }

        public void setStart(Start start) {
            this.start = start;
        }

        public End getEnd() {
            return end;
        }

        public void setEnd(End end) {
            this.end = end;
        }

        public Playlist getPlaylist() {
            return playlist;
        }

        public void setPlaylist(Playlist playlist) {
            this.playlist = playlist;
        }

    }

    @Keep
    public class Tuesday {

        @SerializedName("start")
        @Expose
        private Start start;
        @SerializedName("end")
        @Expose
        private End end;
        @SerializedName("playlist")
        @Expose
        private Playlist playlist;

        public Start getStart() {
            return start;
        }

        public void setStart(Start start) {
            this.start = start;
        }

        public End getEnd() {
            return end;
        }

        public void setEnd(End end) {
            this.end = end;
        }

        public Playlist getPlaylist() {
            return playlist;
        }

        public void setPlaylist(Playlist playlist) {
            this.playlist = playlist;
        }

    }

    @Keep
    public class Wednesday {

        @SerializedName("start")
        @Expose
        private Start start;
        @SerializedName("end")
        @Expose
        private End end;
        @SerializedName("playlist")
        @Expose
        private Playlist playlist;

        public Start getStart() {
            return start;
        }

        public void setStart(Start start) {
            this.start = start;
        }

        public End getEnd() {
            return end;
        }

        public void setEnd(End end) {
            this.end = end;
        }

        public Playlist getPlaylist() {
            return playlist;
        }

        public void setPlaylist(Playlist playlist) {
            this.playlist = playlist;
        }
    }

    @Keep
    public class Saturday {

        @SerializedName("start")
        @Expose
        private Start start;
        @SerializedName("end")
        @Expose
        private End end;
        @SerializedName("playlist")
        @Expose
        private Playlist playlist;

        public Start getStart() {
            return start;
        }

        public void setStart(Start start) {
            this.start = start;
        }

        public End getEnd() {
            return end;
        }

        public void setEnd(End end) {
            this.end = end;
        }

        public Playlist getPlaylist() {
            return playlist;
        }

        public void setPlaylist(Playlist playlist) {
            this.playlist = playlist;
        }

    }

    @Keep
    public class Friday {

        @SerializedName("start")
        @Expose
        private Start start;
        @SerializedName("end")
        @Expose
        private End end;
        @SerializedName("playlist")
        @Expose
        private Playlist playlist;

        public Start getStart() {
            return start;
        }

        public void setStart(Start start) {
            this.start = start;
        }

        public End getEnd() {
            return end;
        }

        public void setEnd(End end) {
            this.end = end;
        }

        public Playlist getPlaylist() {
            return playlist;
        }

        public void setPlaylist(Playlist playlist) {
            this.playlist = playlist;
        }

    }

    @Keep
    public class Monday {

        @SerializedName("start")
        @Expose
        private Start start;
        @SerializedName("end")
        @Expose
        private End end;
        @SerializedName("playlist")
        @Expose
        private Playlist playlist;

        public Start getStart() {
            return start;
        }

        public void setStart(Start start) {
            this.start = start;
        }

        public End getEnd() {
            return end;
        }

        public void setEnd(End end) {
            this.end = end;
        }

        public Playlist getPlaylist() {
            return playlist;
        }

        public void setPlaylist(Playlist playlist) {
            this.playlist = playlist;
        }

    }

    @Keep
    public class Start {

        @SerializedName("hour")
        @Expose
        private Integer hour;
        @SerializedName("min")
        @Expose
        private Integer min;

        public Integer getHour() {
            return hour;
        }

        public void setHour(Integer hour) {
            this.hour = hour;
        }

        public Integer getMin() {
            return min;
        }

        public void setMin(Integer min) {
            this.min = min;
        }

    }

    @Keep
    public class End {

        @SerializedName("hour")
        @Expose
        private Integer hour;
        @SerializedName("min")
        @Expose
        private Integer min;

        public Integer getHour() {
            return hour;
        }

        public void setHour(Integer hour) {
            this.hour = hour;
        }

        public Integer getMin() {
            return min;
        }

        public void setMin(Integer min) {
            this.min = min;
        }

    }

    @Keep
    public class File {

        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("order")
        @Expose
        private Integer order;
        @SerializedName("ext")
        @Expose
        private String ext;
        @SerializedName("time")
        @Expose
        private String time;
        @SerializedName("url")
        @Expose
        private String url;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            this.order = order;
        }

        public String getExt() {
            return ext;
        }

        public void setExt(String ext) {
            this.ext = ext;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    @Keep
    public class Playlist {

        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("files")
        @Expose
        private List<File> files = null;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<File> getFiles() {
            return files;
        }

        public void setFiles(List<File> files) {
            this.files = files;
        }

    }

    @Keep
    public class Defaultplaylist {

        @SerializedName("id")
        @Expose
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

    }
}