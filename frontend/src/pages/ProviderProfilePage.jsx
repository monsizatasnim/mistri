import React from 'react';

const ProviderProfilePage = () => {
    const provider = {
        id: 1,
        name: 'Arafat Hossain',
        category: 'Master Electrician',
        location: 'Dhaka',
        rating: 5.0,
        price: '৳500 / hr',
        isVerified: true,
        reviews: [
            {
                id: 1,
                customerName: 'Arafat Hossain',
                rating: 5,
                comment: 'Excellent service provided!',
                providerReply: 'Thank you for your feedback!',
                createdAt: '2026-10-17'
            }
        ]
    };

    return (
        <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif', backgroundColor: '#f8f9fa', minHeight: '100vh' }}>
            <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)', marginBottom: '20px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <h2 style={{ margin: 0 }}>
                            {provider.name}
                            {provider.isVerified && (
                                <span style={{ background: '#e0f2fe', color: '#0369a1', padding: '4px 10px', borderRadius: '12px', fontSize: '14px', marginLeft: '10px' }}>
                                    ✓ Verified Provider
                                </span>
                            )}
                        </h2>
                        <p style={{ color: '#666', marginTop: '5px' }}>{provider.category} • {provider.location}</p>
                    </div>
                    <button style={{ padding: '10px 20px', background: '#007bff', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>
                        Book Now
                    </button>
                </div>

                <hr style={{ margin: '15px 0', border: '0.5px solid #eee' }} />

                <div style={{ display: 'flex', gap: '40px' }}>
                    <div>
                        <span style={{ color: '#888', fontSize: '14px' }}>Average Rating</span>
                        <h3 style={{ color: '#f59e0b', margin: '5px 0' }}>★ {provider.rating} / 5.0</h3>
                    </div>
                    <div>
                        <span style={{ color: '#888', fontSize: '14px' }}>Starting Price</span>
                        <h3 style={{ color: '#28a745', margin: '5px 0' }}>{provider.price}</h3>
                    </div>
                </div>
            </div>

            <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
                <h3>Customer Reviews ({provider.reviews.length})</h3>
                <hr style={{ margin: '15px 0', border: '0.5px solid #eee' }} />

                {provider.reviews.map((rev) => (
                    <div key={rev.id} style={{ padding: '15px', borderBottom: '1px solid #f0f0f0' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <b>{rev.customerName}</b>
                            <span style={{ color: '#888', fontSize: '12px' }}>{rev.createdAt}</span>
                        </div>
                        <div style={{ color: '#f59e0b', margin: '5px 0' }}>
                            {'★'.repeat(rev.rating)}
                        </div>
                        <p style={{ color: '#333' }}>{rev.comment || "No comment provided."}</p>

                        {rev.providerReply && (
                            <div style={{ background: '#f0f7ff', padding: '10px', borderRadius: '6px', marginTop: '10px', borderLeft: '4px solid #007bff' }}>
                                <small style={{ fontWeight: 'bold', color: '#007bff' }}>Provider Reply:</small>
                                <p style={{ margin: '3px 0 0 0', fontSize: '14px' }}>{rev.providerReply}</p>
                            </div>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
};

export default ProviderProfilePage;