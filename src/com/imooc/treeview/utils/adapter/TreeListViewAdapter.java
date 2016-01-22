package com.imooc.treeview.utils.adapter;

import java.util.List;

import android.R;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.imooc.treeview.utils.Node;
import com.imooc.treeview.utils.TreeHelper;

public abstract class TreeListViewAdapter<T> extends BaseAdapter
{
	protected Context mContext;
	protected List<Node> mAllNodes;
	protected List<Node> mVisibleNodes;
	protected List<Node> rootNodes;
	protected LayoutInflater mInflater;
	

	protected ListView mTree;
	public int currentPosition = -1 ;

	/**
	 * 设置Node的点击回调
	 * 
	 * @author zhy
	 * 
	 */
	public interface OnTreeNodeClickListener
	{
		void onClick(Node node, int position);
	}

	private OnTreeNodeClickListener mListener;

	public void setOnTreeNodeClickListener(OnTreeNodeClickListener mListener)
	{
		this.mListener = mListener;
	}

	public TreeListViewAdapter(ListView tree, Context context, List<T> datas,
			int defaultExpandLevel) throws IllegalArgumentException,
			IllegalAccessException
	{
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mAllNodes = TreeHelper.getSortedNodes(datas, defaultExpandLevel);
		mVisibleNodes = TreeHelper.filterVisiableNodes(mAllNodes);
		rootNodes = TreeHelper.getRootNodes(mVisibleNodes) ;
		mTree = tree;

		mTree.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				expandOrCollapse(position);

				if (mListener != null)
				{
					mListener.onClick(mVisibleNodes.get(position), position);
				}
			}

		});

	}

	/**
	 * 点击搜索或者展开
	 * 
	 * @param position
	 */
	private void expandOrCollapse(int position)
	{
		Node n = mVisibleNodes.get(position);
		if (n != null)
		{
			if (n.isLeaf())
				return;

//			n.setExpand(!n.isExpand());  //允许展开多个
			
			expendSelfAndCloseOther(TreeHelper.getRootNodes(mVisibleNodes),n); //这个方法只展开一个分支
			
			mVisibleNodes = TreeHelper.filterVisiableNodes(mAllNodes);
			notifyDataSetChanged();
		}
	}

	/**
	 * 只允许展开一个
	 * @param n
	 */
	public void expendSelfAndCloseOther(List<Node> rootNodes,Node n)
	{
		boolean temp = n.isExpand() ;
		
		if (n.getParent()==null){  
//			n.setExpand(!temp);
			
			for(Node node :rootNodes)
			{
				if (node!=n) {
					node.setExpand(false);
				}
			}
			n.setExpand(!temp);
			return ;
		}
		
			List<Node> pChild = n.getParent().getChildren() ;
			
			for(Node node :pChild)
			{
				if (node!=n) {
					node.setExpand(false);
				}
			}
		
		n.setExpand(!temp);
	}
	
	@Override
	public int getCount()
	{
		return mVisibleNodes.size();
	}

	@Override
	public Object getItem(int position)
	{
		return mVisibleNodes.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Node node = mVisibleNodes.get(position);
		convertView = getConvertView(node, position, convertView, parent);
		// 设置内边距
		convertView.setPadding(node.getLevel() * 30, 3, 3, 3);
		return convertView;
	}

	public abstract View getConvertView(Node node, int position,
			View convertView, ViewGroup parent);

	
	/**
	 * 动态插入节点
	 * 
	 * @param position
	 * @param string
	 */
	public void addExtraNode(int position, String string)
	{
		Node node = mVisibleNodes.get(position);
		int indexOf = mAllNodes.indexOf(node);
		// Node
		Node extraNode = new Node(-1, node.getId(), string);
		node.setExpand(true);
		
		extraNode.setParent(node);
		node.getChildren().add(extraNode);
		mAllNodes.add(indexOf + 1, extraNode);

		mVisibleNodes = TreeHelper.filterVisiableNodes(mAllNodes);
		notifyDataSetChanged();

	}
}
