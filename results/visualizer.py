import os

import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns

###########################
#### DFA VISUALIZATION ####
###########################

methods = {
    "/random_learnLib",
    "/change_tail_learnLib",
    "/remove_alphabet_learnLib",
    "/add_alphabet_learnLib",
    "/add_state_learnLib",
    "/remove_state_learnLib"
}

states = {
    # "/0005s_10a",
    "/0010s_10a",
    "/0050s_10a",
    "/0100s_10a",
}

base_path = "dfa/data"

EQ_method = "/WP"

base_result_path = "dfa/plots"


def visualize_model_learning_result(df: pd.DataFrame, base_name):
    dirname = os.path.dirname(f"{base_result_path}{base_name}_total.png")
    if not os.path.exists(dirname):
        os.makedirs(dirname)

    df = df.sort_values(by="algorithm")

    # plot difference of queries in  box plot
    diff_df = pd.DataFrame()
    cols = ["eq_count", "mq_count", "distance"]
    for id in list(df["id"]):
        try:
            ttt_row = df[(df["algorithm"] == "dfa/TTT") & (df["id"] == id)].iloc[0][cols].astype(float)
            dynamic_ttt_row = df[(df["algorithm"] == "dfa/dynamicTTT") & (df["id"] == id)].iloc[0][
                cols].astype(float)
            diff = dynamic_ttt_row - ttt_row
            diff["distance"] = dynamic_ttt_row["distance"]
            diff["id"] = id
            diff_df = diff_df.append(diff, ignore_index=True)
        except Exception as e:
            print(e)
    diff_df = diff_df.melt(
        id_vars=["distance", "id"],
        var_name="query_type",
        value_name="query_count"
    )
    ax = sns.catplot(kind="box", y="query_count", col="query_type", data=diff_df,
                     x="distance", sharex=False, sharey=False, legend_out=False)
    ax.savefig(f"{base_result_path}{base_name}_diff_queries.png")

    # plot queries in category box plot
    df = df.melt(
        id_vars=["algorithm", "id", "num_states", "num_alphabet", "distance", "duration"],
        var_name="query_type",
        value_name="query_count")
    ax = sns.catplot(kind="box", hue="algorithm", y="query_count", col="query_type", data=df,
                     x="distance", sharex=False, sharey=False)
    ax.savefig(f"{base_result_path}{base_name}_distance.png")
    ax = sns.catplot(kind="box", y="query_count", x="algorithm", col="query_type", data=df, sharey=False)

    ax.savefig(f"{base_result_path}{base_name}_total.png")
    plt.close()


def visualize_normalized_results(df, base_name):
    dirname = os.path.dirname(f"{base_result_path}{base_name}_total.png")
    if not os.path.exists(dirname):
        os.makedirs(dirname)

    df["norm_eq_count"] = df["eq_count"] / df["num_states"]
    df["norm_mq_count"] = df["mq_count"] / df["num_states"]

    # # plot difference of queries in  box plot
    diff_df = pd.DataFrame()
    cols = ["norm_mq_count", "norm_eq_count", "distance"]
    for id in list(df["id"]):
        try:
            ttt_row = df[(df["algorithm"] == "dfa/TTT") & (df["id"] == id)].iloc[0][cols].astype(float)
            dynamic_ttt_row = df[(df["algorithm"] == "dfa/dynamicTTT") & (df["id"] == id)].iloc[0][
                cols].astype(float)
            diff = dynamic_ttt_row - ttt_row
            diff["distance"] = dynamic_ttt_row["distance"]
            diff["id"] = id
            diff_df = diff_df.append(diff, ignore_index=True)
        except Exception as e:
            print(e)
    diff_df = diff_df.melt(
        id_vars=["distance", "id"],
        var_name="query_type",
        value_name="diff_norm_query_count"
    )
    ax = sns.catplot(kind="box", y="diff_norm_query_count", col="query_type", col_wrap=1, data=diff_df,
                     x="distance", sharex=False, sharey=False, legend_out=False)
    ax.savefig(f"{base_result_path}{base_name}_diff_norm_queries.png")

    # plot queries in category box plot
    df = df[["norm_mq_count", "norm_eq_count", "distance", "id", "algorithm"]].melt(
        id_vars=["id", "distance", "algorithm"],
        var_name="query_type",
        value_name="query_norm_count")
    ax = sns.catplot(kind="box", hue="algorithm", y="query_norm_count", col="query_type", data=df,
                     x="distance", sharex=False, sharey=False)
    ax.savefig(f"{base_result_path}{base_name}_distance.png")
    ax = sns.catplot(kind="box", y="query_norm_count", x="algorithm", col="query_type", data=df,
                     sharey=False)

    ax.savefig(f"{base_result_path}{base_name}_total.png")
    plt.close()


def visualize_dfa_results():
    all_data = pd.DataFrame()
    for method in methods:
        for state in states:
            df = pd.read_csv(base_path + EQ_method + method + state + ".csv")
            visualize_model_learning_result(df, EQ_method + method + state)
            all_data = all_data.append(df, ignore_index=True)
        visualize_normalized_results(all_data, EQ_method + method + "/normalized")


def visualize_dfa_add_state_results():
    df = pd.DataFrame()
    for state in states:
        temp = pd.read_csv(base_path + EQ_method + "/add_state_learnLib" + state + ".csv")
        df = df.append(temp, ignore_index=True)

    df["eq_fre"] = df["eq_count"] / df["distance"]
    df["mq_fre"] = df[""]
    # plot queries in category box plot
    df = df[["norm_mq_count", "norm_eq_count", "distance", "id", "algorithm"]].melt(
        id_vars=["id", "distance", "algorithm"],
        var_name="query_type",
        value_name="query_norm_count")
    ax = sns.catplot(kind="box", hue="algorithm", y="query_norm_count", col="query_type", data=df,
                     x="distance", sharex=False, sharey=False)
    ax.savefig(f"{base_result_path}{base_name}_distance.png")
    ax = sns.catplot(kind="box", y="query_norm_count", x="algorithm", col="query_type", data=df,
                     sharey=False)

    ax.savefig(f"{base_result_path}{base_name}_total.png")
    plt.close()


visualize_dfa_results()
