package com.trasier.client.impl.apache;

import com.trasier.client.Client;
import com.trasier.client.model.Span;

import java.util.List;

public class ApacheRestClient implements Client {


    public ApacheRestClient() {

//        OAuthConsumer consumer = new DefaultOAuthConsumer("key", "secret");
//        OAuthToken token = new DefaultOAuthAccessToken("accesskey", "accesssecret");
//        DefaultHttpClient httpclient = new OAuthHttpClient(consumer, token);
//
//
//        // TODO put all of this stuff into OAuthHttpClient
//        BasicHttpContext localcontext = new BasicHttpContext();
//
//        // Generate OAuth scheme object and stick it in the local
//        // execution context
//        OAuthScheme oauth = new OAuthScheme();
//        localcontext.setAttribute("preemptive-auth", oauth);
//
//        // Add as the first request interceptor
//        httpclient.addRequestInterceptor(new PreemptiveAuth(), 0);
//        httpclient.addRequestInterceptor(new DumpRequest());
//        // TODO end of stuff to put into OAuthHttpClient
//
//        HttpGet httpget = new HttpGet(
//                "http://oauth.term.ie/oauth/example/echo_api.php?foo=bar");
//
//        HttpResponse response = httpclient.execute(httpget,
//                localcontext);
//        HttpEntity entity = response.getEntity();
    }

    @Override
    public boolean sendSpan(Span span) {
        return false;
    }

    @Override
    public boolean sendSpans(List<Span> spans) {
        return false;
    }

    @Override
    public void close() {

    }
}
