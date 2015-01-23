/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 *
 * Created on 13/1/15 11:06 AM
 */
package com.odoo.addons.sale.models;

import android.content.Context;
import android.net.Uri;

import com.odoo.base.addons.res.ResCompany;
import com.odoo.base.addons.res.ResCurrency;
import com.odoo.base.addons.res.ResPartner;
import com.odoo.base.addons.res.ResUsers;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.annotation.Odoo;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.ODate;
import com.odoo.core.orm.fields.types.ODateTime;
import com.odoo.core.orm.fields.types.OFloat;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

import org.json.JSONArray;

import java.util.HashMap;

public class SaleOrder extends OModel {
    public static final String TAG = SaleOrder.class.getSimpleName();
    public static final String AUTHORITY = "com.odoo.core.crm.provider.content.sync.sale_order";
    private Context mContext;
    OColumn name = new OColumn("name", OVarchar.class);
    OColumn date_order = new OColumn("Date", ODateTime.class);
    OColumn partner_id = new OColumn("Customer", ResPartner.class,
            OColumn.RelationType.ManyToOne).setRequired();
    OColumn user_id = new OColumn("Salesperson", ResUsers.class,
            OColumn.RelationType.ManyToOne);
    OColumn amount_total = new OColumn("Total", OFloat.class);
    OColumn validity_date = new OColumn("Expiration Date", ODate.class);
    OColumn payment_term = new OColumn("Payment Term", AccountPaymentTerm.class, OColumn.RelationType.ManyToOne);
    OColumn amount_untaxed = new OColumn("Untaxed", OInteger.class);
    OColumn amount_tax = new OColumn("Tax", OInteger.class);
    OColumn client_order_ref = new OColumn("Client Order Reference",
            OVarchar.class).setSize(100);
    OColumn state = new OColumn("status", OVarchar.class).setSize(10)
            .setDefaultValue("draft");
    @Odoo.Functional(method = "getStateTitle", store = true, depends = {"state"})
    OColumn state_title = new OColumn("State Title", OVarchar.class)
            .setLocalColumn();
    @Odoo.Functional(method = "storePartnerName", store = true, depends = {"partner_id"})
    OColumn partner_name = new OColumn("State Title", OVarchar.class)
            .setLocalColumn();
    OColumn currency_id = new OColumn("currency", ResCurrency.class,
            OColumn.RelationType.ManyToOne);
    @Odoo.Functional(method = "storeCurrencySymbol", store = true, depends = {"currency_id"})
    OColumn currency_symbol = new OColumn("State Title", OVarchar.class)
            .setLocalColumn();
    OColumn order_line = new OColumn("Order Lines", SalesOrderLine.class,
            OColumn.RelationType.OneToMany).setRelatedColumn("order_id");
    @Odoo.Functional(method = "countOrderLines", store = true, depends = {"order_line"})
    OColumn order_line_count = new OColumn("Total Lines", OVarchar.class)
            .setLocalColumn();

    public SaleOrder(Context context, OUser user) {
        super(context, "sale.order", user);
        mContext = context;
        if (getOdooVersion().getVersion_number() == 7) {
            date_order.setType(ODate.class);
        }

    }

    @Override
    public Uri uri() {
        return buildURI(AUTHORITY);
    }

    public ODataRow currency() {
        ResCompany company = new ResCompany(mContext, getUser());
        ODataRow row = company.browse(null, "id = ? ", new String[]{getUser().getCompany_id()});
        if (row != null) {
            return row.getM2ORecord("currency_id").browse();
        }
        return null;
    }

    public String getStateTitle(OValues row) {
        HashMap<String, String> mStates = new HashMap<String, String>();
        mStates.put("draft", "Draft Quotation");
        mStates.put("sent", "Quotation Sent");
        mStates.put("cancel", "Cancelled");
        mStates.put("waiting_date", "Waiting Schedule");
        mStates.put("progress", "Sales Order");
        mStates.put("manual", "Sale to Invoice");
        mStates.put("shipping_except", "Shipping Exception");
        mStates.put("invoice_except", "Invoice Exception");
        mStates.put("done", "Done");
        return mStates.get(row.getString("state"));
    }

    public String storeCurrencySymbol(OValues values) {
        try {
            if (!values.getString("currency_id").equals("false")) {
                JSONArray currency_id = new JSONArray(values.getString("currency_id"));
                return currency_id.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "false";
    }

    public String storePartnerName(OValues values) {
        try {
            if (!values.getString("partner_id").equals("false")) {
                JSONArray partner_id = new JSONArray(values.getString("partner_id"));
                return partner_id.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "false";
    }

    public String countOrderLines(OValues values) {
        try {
            JSONArray order_line = new JSONArray(values.getString("order_line"));
            if (order_line.length() > 0) {
                return " (" + order_line.length() + " lines)";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return " (No lines)";
    }
}
