[![author](https://img.shields.io/badge/author-hglf-blue.svg)](https://github.com/hotstu)
[![Download](https://api.bintray.com/packages/hglf/maven/wxscroller/images/download.svg) ](https://bintray.com/hglf/maven/wxscroller/_latestVersion)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)


wxscroller
=======================
仿微信通讯录分组索引列表

基于ItemDecoration实现， 侵入性小

灵活配置，通过style指定：
1. 侧边栏背景
2. 每一个字母索引的布局
3. 浮动框布局



知识点
1. 如何在非view中使用style、默认style、自定义style参数、获取style指定资源
2. ItemDecoration中处理触摸事件
3. 根据点击事件处理drawable state
4. 脱离view框架的layoutparam获取、拦截、修改，测量绘制


使用：
```groovy
implementation 'github.hotstu.wxscroller:lib:1.0.0'
```
```java
        WXScroller fastScroller = new WXScroller(this);
        fastScroller.attachToRecyclerView(list);
        fastScroller.setScrollerGroupAdapter(new WXScroller.ScrollerGroupAdapter() {
            @Override
            public int getScrollerSize() {
                return 10;
            }

            @Override
            public String getScrollerItem(int index) {
                return index+"";
            }
        });
        fastScroller.setOnScrollerGroupChangeListener(new WXScroller.OnScrollerGroupChangeListener() {
            @Override
            public void onChange(int index, String item) {
                Log.e("onChange", String.format("%d--%s", index, item));
                //TODO mapping group index to adapter position
                LinearLayoutManager layoutManager = (LinearLayoutManager) list.getLayoutManager();
                assert layoutManager != null;
                layoutManager.scrollToPositionWithOffset(index * 10,0);
            }
        });
        fastScroller.show();
```


