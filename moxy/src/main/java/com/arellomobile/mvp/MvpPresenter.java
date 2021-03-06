package com.arellomobile.mvp;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import com.arellomobile.mvp.presenter.PresenterType;
import com.arellomobile.mvp.viewstate.MvpViewState;

/**
 * Date: 15.12.2015
 * Time: 19:31
 *
 * @author Yuri Shmakov
 * @author Alexander BLinov
 * @author Konstantin Tckhovrebov
 */
public abstract class MvpPresenter<View extends MvpView> {
	private boolean mFirstLaunch = true;
	private String mTag;
	private PresenterType mPresenterType;
	private Set<View> mViews;
	private View mViewStateAsView;
	private MvpViewState<View> mViewState;

	public MvpPresenter() {
		Binder.bind(this);

		mViews = Collections.newSetFromMap(new WeakHashMap<View, Boolean>());
	}

	/**
	 * <p>Attach view to view state or to presenter(if view state not exists).</p>
	 * <p>If you use {@link MvpDelegate}, you should not call this method directly.
	 * It will be called on {@link MvpDelegate#onAttach()}, if view does not attached.</p>
	 *
	 * @param view to attachment
	 */
	public void attachView(View view) {
		if (mViewState != null) {
			mViewState.attachView(view);
		} else {
			mViews.add(view);
		}
		if (mFirstLaunch) {
			mFirstLaunch = false;

			onFirstViewAttach();
		}
	}

	/**
	 * <p>Callback after first presenter init and view binding. If this
	 * presenter instance will have to attach some view in future, this method
	 * will not be called.</p>
	 * <p>There you can to interact with {@link #mViewState}.</p>
	 */
	protected void onFirstViewAttach() {
	}

	/**
	 * <p>Detach view from view state or from presenter(if view state not exists).</p>
	 * <p>If you use {@link MvpDelegate}, you should not call this method directly.
	 * It will be called on {@link MvpDelegate#onDetach()}.</p>
	 *
	 * @param view view to detach
	 */
	public void detachView(View view) {
		if (mViewState != null) {
			mViewState.detachView(view);
		} else {
			mViews.remove(view);
		}
	}

	/**
	 * @return views attached to view state, or attached to presenter(if view state not exists)
	 */
	public Set<View> getAttachedViews() {
		if (mViewState != null) {
			return mViewState.getViews();
		}

		return mViews;
	}

	/**
	 * @return view state, casted to view interface for simplify
	 */
	public View getViewState() {
		return mViewStateAsView;
	}

	/**
	 * Check if view is in restore state or not
	 *
	 * @param view view for check
	 * @return true if view state restore state to incoming view. false otherwise.
	 */
	public boolean isInRestoreState(View view) {
		//noinspection SimplifiableIfStatement
		if (mViewState != null) {
			return mViewState.isInRestoreState(view);
		}
		return false;
	}

	/**
	 * Set view state to presenter
	 *
	 * @param viewState that implements type, setted as View generic param
	 */
	@SuppressWarnings("unchecked")
	public void setViewState(MvpViewState<View> viewState) {
		mViewStateAsView = (View) viewState;
		mViewState = (MvpViewState) viewState;
	}

	PresenterType getPresenterType() {
		return mPresenterType;
	}

	void setPresenterType(PresenterType presenterType) {
		mPresenterType = presenterType;
	}

	String getTag() {
		return mTag;
	}

	void setTag(String tag) {
		mTag = tag;
	}

	/**
	 * <p>Called before reference on this presenter will be cleared and instance of presenter
	 * will be never used.</p>
	 */
	public void onDestroy() {
	}

	private static class Binder {
		static void bind(MvpPresenter presenter) {
			ViewStateClassNameProvider viewStateClassNameProvider;
			String viewStateClassNameProviderClassName = presenter.getClass().getName() + "$$ViewStateClassNameProvider";

			//noinspection TryWithIdenticalCatches
			try {
				viewStateClassNameProvider = (ViewStateClassNameProvider) Class.forName(viewStateClassNameProviderClassName).newInstance();
			} catch (ClassNotFoundException e) {
				return;
			} catch (InstantiationException e) {
				throw new RuntimeException("Unable to instantiate " + viewStateClassNameProviderClassName + ": " +
				                           "make sure class name exists, " +
				                           "is public, and " +
				                           "has an empty constructor that is public", e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Unable to instantiate " + viewStateClassNameProviderClassName + ": " +
				                           "make sure class name exists, " +
				                           "is public, and " +
				                           "has an empty constructor that is public", e);
			}

			final String viewStateClassName = viewStateClassNameProvider.getViewStateClassName();

			Object viewState;
			try {
				viewState = Class.forName(viewStateClassName).newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Unable to instantiate " + viewStateClassName + ": " +
				                           "make sure class name exists, " +
				                           "is public, and " +
				                           "has an empty constructor that is public", e);
			}

			presenter.mViewStateAsView = (MvpView) viewState;
			presenter.mViewState = (MvpViewState) viewState;
		}
	}
}
