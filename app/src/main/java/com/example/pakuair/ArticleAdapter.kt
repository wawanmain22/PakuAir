package com.example.pakuair

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArticleAdapter(private val articles: List<Article>) :
    RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ArticleAdapter.ArticleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ArticleViewHolder,
        position: Int
    ) {
       val article = articles[position]
        holder.bind(article)
    }

    override fun getItemCount(): Int = articles.size

    inner class ArticleViewHolder(itemView:View):
            RecyclerView.ViewHolder(itemView) {
                private val titleTextView: TextView = itemView.findViewById(R.id.articleTitle)
                private val descriptionTextView: TextView = itemView.findViewById(R.id.articleDescription)

                fun bind(article: Article) {
                    titleTextView.text = article.title
                    descriptionTextView.text = article.description
                    itemView.setOnClickListener{
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                        itemView.context.startActivity(intent)
                    }
                }
            }
}