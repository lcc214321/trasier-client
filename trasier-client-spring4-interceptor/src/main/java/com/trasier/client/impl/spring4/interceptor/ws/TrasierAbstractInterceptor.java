package com.trasier.client.impl.spring4.interceptor.ws;

import com.trasier.client.TrasierConstants;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;
import org.w3c.dom.Node;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

public abstract class TrasierAbstractInterceptor {
    protected String extractOutgoingEndpointName(MessageContext messageContext, Object endpoint) {
        if (messageContext.getRequest() instanceof SoapMessage) {
            SoapMessage soapMessage = (SoapMessage) messageContext.getRequest();
            SoapBody body = soapMessage.getSoapBody();
            if (body.getPayloadSource() instanceof DOMSource) {
                Node node = ((DOMSource) body.getPayloadSource()).getNode();
                return node.getPrefix();
            }
        }

        return TrasierConstants.UNKNOWN;
    }

    protected String extractOperationName(MessageContext messageContext, Object endpoint) {
        if (messageContext.getRequest() instanceof SoapMessage) {
            SoapMessage soapMessage = (SoapMessage) messageContext.getRequest();

            String soapAction = soapMessage.getSoapAction();
            SoapBody body = soapMessage.getSoapBody();
            if (body.getPayloadSource() instanceof DOMSource) {
                Node node = ((DOMSource) body.getPayloadSource()).getNode();
                return node.getLocalName();
            } else if(!StringUtils.isEmpty(soapAction)) {
                soapAction = soapAction.replaceAll("\"", "");
                String[] soapActionArray = soapAction.split("/");
                return soapActionArray[soapActionArray.length - 1];
            }
        }

        return extractOperationName(endpoint);
    }

    protected String extractOperationName(Object endpoint) {
        if (endpoint instanceof MethodEndpoint) {
            return ((MethodEndpoint) endpoint).getMethod().getName();
        }

        return TrasierConstants.UNKNOWN;
    }
}