package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.BlocksConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.BlockFinderController;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.apache.http.HttpException;
import org.apache.http.MethodNotSupportedException;

import java.util.ArrayList;
import java.util.List;

/**
 * 块配置API类
 */
public class BlocksConfigApi extends ApiBase {

    @Override
    public String getRoute() {
        return "blocks";
    }

    /**
     * 获取当前块配置信息
     * @return JSON格式的块配置数据
     */
    @Override
    public synchronized String get() {
        Object[] result;
        var list = ConfigStore.instance.getConfig().blocks.getBlockConfigs();
        result = list.stream().toArray();
        return gson.toJson(result);
    }

    /**
     * 处理POST请求，更新或创建块配置
     * @param body JSON格式的BlockEspConfig对象
     * @return 更新后的BlockEspConfig对象的JSON格式数据
     * @throws MethodNotSupportedException 如果接收到空配置或者配置存在冲突时抛出异常
     */
    @Override
    public synchronized String post(String body) throws MethodNotSupportedException {
        BlockEspConfig jsonConfig = gson.fromJson(body, BlockEspConfig.class);
        if (jsonConfig.blocks.size() == 0) {
            throw new MethodNotSupportedException("接收到空的BlockEspConfig对象");
        }

        BlocksConfig blocksConfig = ConfigStore.instance.getConfig().blocks;
        BlockEspConfig config = blocksConfig.findExact(jsonConfig.blocks);
        if (config != null) {
            config.copyFrom(jsonConfig);
        } else {
            List<BlockEspConfig> configs = new ArrayList<>();
            for (Block block: jsonConfig.blocks) {
                config = blocksConfig.find(block);
                if (config != null && !configs.contains(config)) {
                    configs.add(config);
                }
            }

            if (configs.size() > 1) {
                throw new MethodNotSupportedException("接收到包含多个已有配置中块信息的BlockEspConfig对象");
            }

            if (configs.size() == 1) {
                config = configs.get(0);
                config.blocks = jsonConfig.blocks;
                config.copyFrom(jsonConfig);
                blocksConfig.refreshMap();
                // 重新添加会导致扫描
                BlockFinderController.instance.removeConfig(config);
                BlockFinderController.instance.addConfig(config);
            } else {
                config = BlockEspConfig.createDefault(jsonConfig.blocks);
                blocksConfig.add(config);
            }
        }

        ConfigStore.instance.requestWrite();

        return gson.toJson(config);
    }

    /**
     * 处理DELETE请求，删除指定ID的块配置
     * @param id 配置对应的块ID
     * @return 删除操作成功后返回的消息
     * @throws MethodNotSupportedException 如果找不到对应ID的块或者配置不存在时抛出异常
     */
    @Override
    public synchronized String delete(String id) throws MethodNotSupportedException {
        ResourceLocation loc = new ResourceLocation(id);
        Block block = Registries.BLOCKS.getValue(loc);
        if (block == null) {
            throw new MethodNotSupportedException("无法通过ID找到对应的块");
        }

        BlocksConfig blocksConfig = ConfigStore.instance.getConfig().blocks;
        BlockEspConfig config = blocksConfig.find(block);
        if (config != null) {
            blocksConfig.remove(config);
        } else {
            throw new MethodNotSupportedException("该块没有对应的配置");
        }

        ConfigStore.instance.requestWrite();

        return "{ ok: true }";
    }

    /**
     * 添加块配置的内部类
     */
    public static class Add extends ApiBase {

        @Override
        public String getRoute() {
            return "blocks-add";
        }

        /**
         * 根据ID添加新的块到配置中
         * @param id 要添加的块的ID
         * @return 新增块配置的JSON格式数据
         * @throws HttpException 如果找不到对应的块或者块已存在于其他配置中，则抛出异常
         */
        @Override
        public String post(String id) throws HttpException {
            ResourceLocation loc = new ResourceLocation(id);
            Block block = Registries.BLOCKS.getValue(loc);
            if (block == null) {
                throw new MethodNotSupportedException("无法通过ID找到对应的块");
            }

            BlocksConfig blocksConfig = ConfigStore.instance.getConfig().blocks;
            if (blocksConfig.find(block) != null) {
                throw new MethodNotSupportedException("选择的块已存在于其他BlockEspConfig中");
            }

            BlockEspConfig config = BlockEspConfig.createDefault(ImmutableList.from(block));
            blocksConfig.add(config);

            ConfigStore.instance.requestWrite();

            return gson.toJson(config);
        }
    }
}