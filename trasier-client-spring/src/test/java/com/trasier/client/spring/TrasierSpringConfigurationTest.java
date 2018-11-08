package com.trasier.client.spring;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TrasierSpringConfigurationTest {
    @Test
    public void getterSetterTest() {
        TrasierSpringConfiguration sut = new TrasierSpringConfiguration();

        sut.setQueueSize(1);
        sut.setQueueDelay(2);
        sut.setMaxTaskCount(3);
        sut.setMaxSpansPerTask(4);
        sut.setQueueSizeErrorThresholdMultiplicator(5);

        assertEquals(1, sut.getQueueSize());
        assertEquals(2, sut.getQueueDelay());
        assertEquals(3, sut.getMaxTaskCount());
        assertEquals(4, sut.getMaxSpansPerTask());
        assertEquals(5, sut.getQueueSizeErrorThresholdMultiplicator());

    }
}