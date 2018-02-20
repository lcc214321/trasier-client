package com.trasier.client.impl.spring;

import com.trasier.client.TrasierClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by lukasz on 05.02.18.
 */
@Component
public class TrasierSpringClient extends TrasierClient {

    @Autowired
    public TrasierSpringClient(SpringRestClient restClient) {
        super(restClient);
    }

}
