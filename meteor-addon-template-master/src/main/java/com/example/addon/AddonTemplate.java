package com.example.addon;

import com.example.addon.modules.CustomRefillDetector;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class Addon extends MeteorAddon {
    public static final Category CATEGORY = new Category("Base Detector");

    @Override
    public void onInitialize() {
        Modules.get().add(new CustomRefillDetector(CATEGORY));
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }
}