package com.okbatech.smartevents.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.jivesoftware.smack.ConnectionConfiguration
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import javax.inject.Singleton

const val XMPP_DOMAIN = "evenro.duckdns.org"
const val XMPP_MUC_DOMAIN = "conference.$XMPP_DOMAIN"

/**
 * STARTTLS on 5222, not the "direct TLS" 5223 port ejabberd also exposes — Smack's
 * XMPPTCPConnection targets the standard RFC 6120 STARTTLS negotiation out of the box;
 * implicit/legacy TLS on 5223 would need extra verification of Smack's support before
 * relying on it, so 5222+STARTTLS is the safer default here. 5223 stays open server-side
 * for other standard XMPP tooling.
 */
private const val XMPP_PORT = 5222

@Module
@InstallIn(SingletonComponent::class)
object XmppModule {

    @Provides
    @Singleton
    fun provideXmppConnectionConfiguration(): XMPPTCPConnectionConfiguration =
        XMPPTCPConnectionConfiguration.builder()
            .setXmppDomain(XMPP_DOMAIN)
            .setHost(XMPP_DOMAIN)
            .setPort(XMPP_PORT)
            .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
            .build()

    @Provides
    @Singleton
    fun provideXmppConnection(config: XMPPTCPConnectionConfiguration): XMPPTCPConnection =
        XMPPTCPConnection(config)
}
