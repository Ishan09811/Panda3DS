package com.panda3ds.pandroid.view.gamesgrid;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import com.panda3ds.pandroid.R;
import com.panda3ds.pandroid.data.game.GameMetadata;

class ItemHolder extends RecyclerView.ViewHolder {
    public ItemHolder(@NonNull View itemView) {
        super(itemView);
    }

    new Handler().postDelayed(new Runnable() {
    @Override
    public void run() {
        // Set the ellipsize property and make the TextViews selectable to start the marquee
        ((AppCompatTextView) itemView.findViewById(R.id.title)).setEllipsize(TextUtils.TruncateAt.MARQUEE);
        ((AppCompatTextView) itemView.findViewById(R.id.title)).setSelected(true);

        ((AppCompatTextView) itemView.findViewById(R.id.description)).setEllipsize(TextUtils.TruncateAt.MARQUEE);
        ((AppCompatTextView) itemView.findViewById(R.id.description)).setSelected(true);
    }
}, 3000);

    public void apply(GameMetadata game) {
        ((AppCompatTextView) itemView.findViewById(R.id.title))
                .setText(game.getTitle());
        ((GameIconView) itemView.findViewById(R.id.icon))
                .setImageBitmap(game.getIcon());
        ((AppCompatTextView) itemView.findViewById(R.id.description))
                .setText(game.getPublisher());
    }
}
