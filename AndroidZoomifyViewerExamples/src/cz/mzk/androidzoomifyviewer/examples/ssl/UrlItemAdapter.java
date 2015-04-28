package cz.mzk.androidzoomifyviewer.examples.ssl;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import cz.mzk.androidzoomifyviewer.examples.R;
import cz.mzk.androidzoomifyviewer.examples.ssl.HttpRequestTask.ResultHandler;
import cz.mzk.androidzoomifyviewer.examples.ssl.UrlItemAdapter.UrlViewHolder;

public class UrlItemAdapter extends Adapter<UrlViewHolder> {

	class UrlViewHolder extends ViewHolder implements OnClickListener {

		private final TextView url;
		private final TextView info;

		public UrlViewHolder(View itemView) {
			super(itemView);
			itemView.setOnClickListener(this);
			url = (TextView) itemView.findViewById(R.id.url);
			info = (TextView) itemView.findViewById(R.id.info);
		}

		public void bind(UrlItem item) {
			url.setText(item.getUrl());
			info.setText(item.getInfo());
		}

		@Override
		public void onClick(View v) {
			// TODO: http request to url, results in toast
			new HttpRequestTask(context, new ResultHandler() {

				@Override
				public void onSuccess() {
					if (toast != null) {
						toast.cancel();
					}
					toast = Toast.makeText(context, "success", Toast.LENGTH_SHORT);
					toast.show();
				}

				@Override
				public void onError(String message) {
					if (toast != null) {
						toast.cancel();
					}
					toast = Toast.makeText(context, "error: " + message, Toast.LENGTH_LONG);
					toast.show();
				}
			}).execute(url.getText().toString());
		}
	}

	private final Context context;
	private Toast toast;

	public UrlItemAdapter(Context context) {
		this.context = context;
	}

	private final List<UrlItem> data = UrlItem.getTestData();

	@Override
	public int getItemCount() {
		return data.size();
	}

	@Override
	public void onBindViewHolder(UrlViewHolder holder, int position) {
		holder.bind(data.get(position));
	}

	@Override
	public UrlViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ssl_test, parent, false);
		return new UrlViewHolder(root);
	}

}
