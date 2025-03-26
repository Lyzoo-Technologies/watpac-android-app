package text.attendance.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class BuilderAdapter extends RecyclerView.Adapter<BuilderAdapter.ViewHolder> {
    private List<Builder> builderList;
    private List<Builder> fullList;

    public BuilderAdapter(List<Builder> builderList) {
        this.builderList = builderList;
        this.fullList = new ArrayList<>(builderList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(builderList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return builderList.size();
    }

    public void filter(String text) {
        builderList.clear();
        if (text.isEmpty()) {
            builderList.addAll(fullList);
        } else {
            text = text.toLowerCase();
            for (Builder builder : fullList) {
                if (builder.getName().toLowerCase().contains(text)) {
                    builderList.add(builder);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
