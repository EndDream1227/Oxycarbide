package com.kaze943.oxycarbide.market;

import com.kaze943.oxycarbide.shop.ShopItemDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Editable frame of the global market GUI, loaded from
 * {@code config/oxycarbide/globalshop_default.json}.
 *
 * <p>The top and bottom rows are free for decoration (only {@code redirect} and
 * {@code placeholder} types); the two bottom corners are the page buttons and are
 * configured through {@link #previous} and {@link #next}.</p>
 */
public final class MarketLayout {
	public String title = "世界市场";
	public int rows = 6;
	public ShopItemDefinition previous = new ShopItemDefinition();
	public ShopItemDefinition next = new ShopItemDefinition();
	public List<ShopItemDefinition> frame = new ArrayList<>();
}
