package vc.rux.localnode.common

import vc.rux.localnode.NodeMode

internal val NodeMode.idPrefix
    get() = when (this) {
        is NodeMode.Fork -> 'f'
        is NodeMode.Local -> 'l'
    }
