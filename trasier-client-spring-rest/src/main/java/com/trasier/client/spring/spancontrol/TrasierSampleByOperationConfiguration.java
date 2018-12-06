package com.trasier.client.spring.spancontrol;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TrasierSampleByOperationConfiguration {

    private List<String> whitelist = new ArrayList<>();
    private List<String> blacklist = new ArrayList<>();

}
