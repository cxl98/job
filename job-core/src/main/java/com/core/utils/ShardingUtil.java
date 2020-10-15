package com.core.utils;

public class ShardingUtil {
    private static InheritableThreadLocal<ShardingVO>contextHolder=new InheritableThreadLocal<>();
    public static class ShardingVO{
        private int index;
        private int total;

        public ShardingVO(int index, int total) {
            this.index = index;
            this.total = total;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }
    }
    public static void setShadingVo(ShardingVO shadingVo){
        contextHolder.set(shadingVo);
    }
    public static ShardingVO getShardingVo(){
        return contextHolder.get();
    }

}
