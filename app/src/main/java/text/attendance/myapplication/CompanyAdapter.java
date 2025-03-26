package text.attendance.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.ViewHolder> {
    private List<String> companyList;
    private List<String> fullList;

    public CompanyAdapter(List<String> companyList) {
        this.companyList = companyList;
        this.fullList = new ArrayList<>(companyList);
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
        holder.textView.setText(companyList.get(position));
    }

    @Override
    public int getItemCount() {
        return companyList.size();
    }

    public void filter(String text) {
        companyList.clear();
        if (text.isEmpty()) {
            companyList.addAll(fullList);
        } else {
            text = text.toLowerCase();
            for (String company : fullList) {
                if (company.toLowerCase().contains(text)) {
                    companyList.add(company);
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

