package org.bakingprocess.registry;

import org.bakingprocess.block.process.playeraction.PlayerActionFactory;
import org.bakingprocess.block.process.playeraction.impl.*;

public class PlayerActions {

    public static void registerDefaults() {
        // 添加物品操作
        PlayerActionFactory.register(
                AddItemPlayerAction.TYPE,
                AddItemPlayerAction::fromParams,
                context -> AddItemPlayerAction.fromContext(context).orElse(null)
        );
        // 添加内容物操作
        PlayerActionFactory.register(
                AddContentPlayerAction.TYPE,
                AddContentPlayerAction::fromParams,
                context -> AddContentPlayerAction.fromContext(context).orElse(null)
        );
    }
}
