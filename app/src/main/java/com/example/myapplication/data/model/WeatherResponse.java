package com.example.myapplication.data.model;

import java.util.List;


public class WeatherResponse {

    public long id;
    public Coord coord;
    public List<Weather> weather;
    public String base;
    public Main main;
    public int visibility;
    public Wind wind;
    public Clouds clouds;
    public long dt;
    public Sys sys;
    public int timezone;

    public String name;
    public int cod;
}
