package vc.rux.pokefork.common

import vc.rux.pokefork.NodeMode

internal val NodeMode.idPrefix
    get() = when (this) {
        is NodeMode.Fork -> 'f'
        is NodeMode.Local -> 'l'
    }
