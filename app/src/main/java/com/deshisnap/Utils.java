package com.foodcafe.myapplication;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;

import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Utils  {
    public static void applyGradientToText(TextView textView, String startColor, String endColor) {
        float textWidth = textView.getPaint().measureText(textView.getText().toString());

        Shader textShader = new LinearGradient(
                0, 0, textWidth, 0,
                new int[]{
                        Color.parseColor(startColor),
                        Color.parseColor(endColor)
                }, null, Shader.TileMode.CLAMP
        );

        textView.getPaint().setShader(textShader);
    }


}

