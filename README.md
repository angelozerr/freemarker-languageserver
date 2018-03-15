[![Build Status](https://secure.travis-ci.org/angelozerr/freemarker-languageserver.png)](http://travis-ci.org/angelozerr/freemarker-languageserver)

Freemarker Language Server
===========================

The Freemarker Language Server is a Freemarker language specific implementation of the [Language Server Protocol](https://github.com/Microsoft/language-server-protocol)
and can be used with any editor that supports the protocol, to offer good support for the Freemarker Language. The server is based on:

* [Eclipse LSP4J](https://github.com/eclipse/lsp4j), the Java binding for the Language Server Protocol,
 * Freemarker 

Features
--------------
* As you type reporting of parsing errors

Clients
-------
This repository only contains the server implementation. Here are some known clients consuming this server:

* [freemarker-lsp4e](https://github.com/angelozerr/freemarker-lsp4e) : an extension for Eclipse