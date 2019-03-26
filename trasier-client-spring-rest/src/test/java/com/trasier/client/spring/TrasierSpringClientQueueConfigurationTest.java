package com.trasier.client.spring;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TrasierSpringClientQueueConfigurationTest {
    @Test
    public void getterSetterTest() {
        TrasierSpringClientQueueConfiguration sut = new TrasierSpringClientQueueConfiguration();

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