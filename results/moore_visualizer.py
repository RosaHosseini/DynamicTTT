import os

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns


def visualize_model_learning_result(df: pd.DataFrame, name):
    basePath2 = "moore/plots"
    df = df.drop(columns=["date", "duration"])
    dirname = os.path.dirname(f"{basePath2}{name}_total.png")
    if not os.path.exists(dirname):
        os.makedirs(dirname)

    df = df.sort_values(by="distance")

    diff_df = pd.DataFrame()
    cols = ["eq_count", "mq_count", "distance"]
    for id in np.unique(df["id"]):
        for distance in np.unique(df["distance"]):
            try:
                ttt_row = \
                df[(df["algorithm"] == "mealy/TTT") & (df["id"] == id) & (df["distance"] == distance)].iloc[
                    0][cols].astype(int)
                dynamic_ttt_row = df[(df["algorithm"] == "mealy/dynamicTTT") & (df["id"] == id) & (
                            df["distance"] == distance)].iloc[0][
                    cols].astype(int)
                diff = dynamic_ttt_row - ttt_row
                diff["distance"] = distance
                diff_df = diff_df.append(diff, ignore_index=True)
            except Exception as e:
                continue
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
    for axes in ax.axes.flat:
        _ = axes.set_xticklabels(axes.get_xticklabels(), rotation=90)
    ax.savefig(f"{basePath2}{name}_distance.png")

    ax = sns.catplot(kind="box", y="query_count", x="algorithm", col="query_type", data=df, col_wrap=1,
                     sharey=False)

    ax.savefig(f"{basePath2}{name}_total.png")
    plt.close()


###########################
#### MOORE VISUALIZATION ####
###########################


def visualize_moore_results():
    basePath = "moore/data"

    for eqMethod in ["/WP", "/W"]:
        df = pd.read_csv(basePath + eqMethod + "/OPEN_SSL_CLIENT.csv")
        visualize_model_learning_result(df, eqMethod + "/OPEN_SSL_CLIENT")

        df = pd.read_csv(basePath + eqMethod + "/OPEN_SSL_SERVER.csv")
        visualize_model_learning_result(df, eqMethod + "/OPEN_SSL_SERVER")


visualize_moore_results()
