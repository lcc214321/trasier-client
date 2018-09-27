package com.trasier.client.impl.spring.interceptor.ws;

import com.trasier.client.TrasierConstants;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;
import org.w3c.dom.Node;

import javax.xml.transform.dom.DOMSource;

// TODO: Pull out into extractors
public abstract class TrasierAbstractInterceptor {

    protected String extractOutgoingEndpointName(MessageContext messageContext) {
        if (messageContext.getRequest() instanceof SoapMessage) {
            SoapMessage soapMessage = (SoapMessage) messageContext.getRequest();
            SoapBody body = soapMessage.getSoapBody();
            if (body.getPayloadSource() instanceof DOMSource) {
                Node node = ((DOMSource) body.getPayloadSource()).getNode();
                if (!StringUtils.isEmpty(node.getPrefix())) {
                    return node.getPrefix();
                }
                String namespace = node.getNamespaceURI();
                if (!StringUtils.isEmpty(namespace)) {
                    String[] soapActionArray = namespace.split("/");
                    return soapActionArray[soapActionArray.length - 1];
                }
            }
        }

        return null;
    }

    protected String extractOperationName(MessageContext messageContext, Object endpoint) {
        if (messageContext.getRequest() instanceof SoapMessage) {
            SoapMessage soapMessage = (SoapMessage) messageContext.getRequest();

            String soapAction = soapMessage.getSoapAction();
            SoapBody body = soapMessage.getSoapBody();
            if (body.getPayloadSource() instanceof DOMSource) {
                Node node = ((DOMSource) body.getPayloadSource()).getNode();
                return node.getLocalName();
            } else if (!StringUtils.isEmpty(soapAction)) {
                soapAction = soapAction.replaceAll("\"", "");
                String[] soapActionArray = soapAction.split("/");
                return soapActionArray[soapActionArray.length - 1];
            }
        }

        return extractOperationName(endpoint);
    }

    private String extractOperationName(Object endpoint) {
        if (endpoint instanceof MethodEndpoint) {
            return ((MethodEndpoint) endpoint).getMethod().getName();
        }

        return TrasierConstants.UNKNOWN_OPERATION;
    }
}