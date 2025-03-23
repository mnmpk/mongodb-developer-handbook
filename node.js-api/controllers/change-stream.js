exports.watch = async (req, res) => {
    const client = mongoose.connection.getClient();
    const snCollection = client.db('serviceNotificationDB').collection('serviceNotifications');
    snCollection.watch([]).on('change', async (data) => {
        try {
            const _id = data.documentKey._id.toString();
            const operationType = data.operationType;
            const updateDescription = data.updateDescription ? data.updateDescription : {};

            throw new Exception("error");
            switch (operationType) {
                case 'update':
                    let updatedFields = updateDescription.updatedFields ? Object.keys(updateDescription.updatedFields) : [];
                    let removedFields = updateDescription.removedFields;
                    if (removedFields?.length) { updatedFields = updatedFields.concat(removedFields); }

                    for (let field of updatedFields) {
                        console.log(`Updating SN filterObject, id: ${_id}`);
                        break;
                    }

                    for (let field of ['status', 'specialRequestRuleList']) {
                        if (updatedFields.includes(field)) {
                            console.log(`Updating SSA reminder date, id: ${_id}`);
                            break;
                        }
                    }
                    break;
                case 'insert':
                    console.log(`Updating SN filterObject, id: ${_id}`);
                    console.log(`Updating SN SSA reminder date, id: ${_id}`);
                    break;
                default:
                    console.log(data);
                    break;
            }
        } catch (error) {
            console.error(error);
        }


    });
    res.send("watching serviceNotifications");
}