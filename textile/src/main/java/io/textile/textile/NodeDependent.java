package io.textile.textile;

import mobile.Mobile_;

abstract class NodeDependent {

    Mobile_ node;

    public NodeDependent(Mobile_ node) {
        this.node = node;
    }
}