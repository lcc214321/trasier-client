package com.trasier.client.impl.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;
import java.util.TreeSet;

public class TrasierHandler implements SOAPHandler<SOAPMessageContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrasierHandler.class);

    @Override
    public Set<QName> getHeaders() {
        return new TreeSet<>();
    }

    @Override
    public boolean handleMessage(SOAPMessageContext smc) {
        log(smc);

        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext smc) {
        log(smc);

        return true;
    }

    @Override
    public void close(MessageContext messageContext) {
    }

    private void log(SOAPMessageContext smc) {
//        Span span = spanAccessor.getCurrentSpan();
//
//        if ((Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
//            span = tracer.createSpan("", span);
//        } else {
//        }
//
//        try {
//
//
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            smc.getMessage().writeTo(out);
//            String nachricht = prettyFormat(out.toString());
//
//            try {
//                .log(logInfo, nachricht);
//            } catch (TaskRejectedException | SchedulingException e) {
//                LOGGER.error(e, e);
//            }
//
//            span.tag("conversationId
//            span.tag("sender", logIn
//            span.tag("empfaenger", l
//            span.tag("operationName"
//
//            if ((Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
//                span.tag("requestMessage", nachricht);
//            } else {
//                span.tag("responseMessage", nachricht);
//                tracer.close(span);
//            }
//
//        } catch (Exception e) {
//            LOGGER.warn(e.getMessage(), e);
//        }
    }
}