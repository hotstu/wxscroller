package github.hotstu.wxscrollerdemo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import github.hotstu.naiue.widget.recycler.MOCommonViewHolder;
import github.hotstu.naiue.widget.recycler.MOTypedRecyclerAdapter;
import github.hotstu.wxscroller.WXScroller;


public class MainActivity extends AppCompatActivity {
    private RecyclerView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = findViewById(R.id.list);
        LinearLayoutManager layout = new LinearLayoutManager(this);

        MOTypedRecyclerAdapter adapter = new MOTypedRecyclerAdapter();
        adapter.addDelegate(new MOTypedRecyclerAdapter.AdapterDelegate() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(MOTypedRecyclerAdapter adapter, ViewGroup parent) {
                View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                return new MOCommonViewHolder(view);
            }

            @Override
            public void onBindViewHolder(MOTypedRecyclerAdapter adapter, RecyclerView.ViewHolder holder, Object data) {
                ((MOCommonViewHolder) holder).setText(android.R.id.text1, ((String) data));
            }

            @Override
            public boolean isDelegateOf(Class<?> clazz, Object item, int position) {
                return true;
            }
        });

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
        fastScroller.show();
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add("" + i);
        }

        adapter.addItems(items);
        //adapter.addItems(Arrays.asList("1", "2"));
        list.setAdapter(adapter);
        list.setLayoutManager(layout);
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

    }

}
