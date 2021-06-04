import os

import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns


def visualize_model_learning_result(df: pd.DataFrame, name):
    basePath2 = "dfa/plots"
    dirname = os.path.dirname(f"{basePath2}{name}_total.png")
    if not os.path.exists(dirname):
        os.makedirs(dirname)

    df = df.sort_values(by="algorithm")

    diff_df = pd.DataFrame()
    cols = ["eq_count", "mq_count", "distance"]
    for id in list(df["id"]):
        try:
            ttt_row = df[(df["algorithm"] == "dfa/TTT") & (df["id"] == id)].iloc[0][cols].astype(int)
            dynamic_ttt_row = df[(df["algorithm"] == "dfa/dynamicTTT") & (df["id"] == id)].iloc[0][
                cols].astype(int)
            diff = dynamic_ttt_row - ttt_row
            diff["distance"] = dynamic_ttt_row["distance"]
            diff_df = diff_df.append(diff, ignore_index=True)
        except Exception as e:
            print(e)
    diff_df = diff_df.melt(
        id_vars=["distance"],
        var_name="query_type",
        value_name="query_count"
    )
    ax = sns.catplot(kind="box", y="query_count", col="query_type", col_wrap=1, data=diff_df,
                     x="distance", sharex=False, sharey=False, legend_out=False)
    ax.savefig(f"{basePath2}{name}_delta_distance.png")

    df = df.melt(
        id_vars=["algorithm", "id", "num_states", "num_alphabet", "distance"],
        var_name="query_type",
        value_name="query_count")
    ax = sns.catplot(kind="box", hue="algorithm", y="query_count", col="query_type", col_wrap=1, data=df,
                     x="distance", sharex=False, sharey=False)
    ax.savefig(f"{basePath2}{name}_distance.png")
    ax = sns.catplot(kind="box", y="query_count", x="algorithm", col="query_type", data=df, col_wrap=1,
                     sharey=False)

    ax.savefig(f"{basePath2}{name}_total.png")
    plt.close()


###########################
#### DFA VISUALIZATION ####
###########################


def visualize_dfa_results():
    basePath = "dfa/data"

    methods = {
        "/random_learnLib",
        "/change_tail_learnLib",
        "/remove_alphabet_learnLib",
        "/add_alphabet_learnLib",
        "/add_state_learnLib",
        "/remove_state_learnLib"
    }

    eqMethod = "/WP"

    for method in methods:
        df = pd.read_csv(basePath + eqMethod + method + "/0005s_5a.csv")
        visualize_model_learning_result(df, eqMethod + method + "/0005s_5a")

        df = pd.read_csv(basePath + eqMethod + method + "/0010s_20a.csv")
        visualize_model_learning_result(df, eqMethod + method + "/0010s_20a")

        df = pd.read_csv(basePath + eqMethod + method + "/0050s_20a.csv")
        visualize_model_learning_result(df, eqMethod + method + "/0050s_20a")


visualize_dfa_results()
